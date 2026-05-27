# Architecture

## Overview

Spring PetClinic Microservices is a distributed implementation of the classic PetClinic application built with Spring Boot 3 and Spring Cloud. The system decomposes the monolithic PetClinic into independently deployable services that communicate through a service registry and are exposed to clients via a single API gateway.

## Services

| Service | Image / Build | Host Port | Container Port | Role |
|---|---|---|---|---|
| `config-server` | `springcommunity/spring-petclinic-config-server` | 8888 | 8888 | Infrastructure |
| `discovery-server` | `springcommunity/spring-petclinic-discovery-server` | 8761 | 8761 | Infrastructure |
| `customers-service` | `springcommunity/spring-petclinic-customers-service` | 8081 | 8081 | Domain |
| `visits-service` | `springcommunity/spring-petclinic-visits-service` | 8082 | 8082 | Domain |
| `vets-service` | `springcommunity/spring-petclinic-vets-service` | 8083 | 8083 | Domain |
| `genai-service` | `springcommunity/spring-petclinic-genai-service` | 8084 | 8084 | Domain |
| `api-gateway` | `springcommunity/spring-petclinic-api-gateway` | 8080 | 8080 | API Layer |
| `tracing-server` | `openzipkin/zipkin` | 9411 | 9411 | Observability |
| `admin-server` | `springcommunity/spring-petclinic-admin-server` | 9090 | 9090 | Observability |
| `grafana-server` | Built from `./docker/grafana` | 3030 | 3000 | Observability |
| `prometheus-server` | Built from `./docker/prometheus` | 9091 | 9090 | Observability |

## Service Discovery

The project uses **Spring Cloud Netflix Eureka** for service discovery. The `discovery-server` (port 8761) acts as the Eureka Server. Every Spring Boot service registers itself with Eureka on startup. The API gateway resolves service locations at runtime using the `lb://` URI scheme (e.g. `lb://vets-service`), which delegates to the Spring Cloud load-balancer to look up instances from the Eureka registry and distribute requests across them.

## Configuration Management

Centralized configuration is provided by **Spring Cloud Config Server** (`config-server` on port 8888). All services import their configuration from the Config Server at startup via:

```yaml
spring.config.import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
```

The Config Server can serve properties from a Git repository or the local filesystem (using the `native` profile). This allows environment-specific configuration to be managed in a single place and pushed to all services without redeployment.

## API Gateway Routing

The `api-gateway` service (port 8080) is the single entry point for external clients. It uses Spring Cloud Gateway to route requests to downstream services. Default filters include a `CircuitBreaker` (with a `/fallback` URI) and a `Retry` filter for `SERVICE_UNAVAILABLE` responses.

| Path Pattern | Target Service | Filter |
|---|---|---|
| `/api/vet/**` | `vets-service` | `StripPrefix=2` |
| `/api/visit/**` | `visits-service` | `StripPrefix=2` |
| `/api/customer/**` | `customers-service` | `StripPrefix=2` |
| `/api/genai/**` | `genai-service` | `StripPrefix=2` |

`StripPrefix=2` removes the first two path segments (e.g. `/api/vet`) before forwarding, so `/api/vet/specialties` becomes `/specialties` on the target service.

## Startup Order

Service startup is controlled by Docker Compose `depends_on` with health-check conditions:

1. **`config-server`** starts first (no dependencies).
2. **`discovery-server`** starts after `config-server` is healthy.
3. **Domain and application services** (`customers-service`, `visits-service`, `vets-service`, `genai-service`, `api-gateway`, `admin-server`) start after both `config-server` and `discovery-server` are healthy.
4. **Observability services** (`tracing-server`, `grafana-server`, `prometheus-server`) have no startup dependencies and can start in parallel with everything else.

## Observability

| Tool | Service | Port | Purpose |
|---|---|---|---|
| **Zipkin** | `tracing-server` | 9411 | Distributed tracing — collects and visualizes request traces across services |
| **Spring Boot Admin** | `admin-server` | 9090 | Monitoring dashboard for all registered Spring Boot applications |
| **Grafana** | `grafana-server` | 3030 → 3000 | Metrics visualization and dashboards |
| **Prometheus** | `prometheus-server` | 9091 → 9090 | Metrics collection and time-series storage |

Services export metrics via Micrometer and propagate trace context so that end-to-end request flows are visible in Zipkin.

## Docker

All Spring Boot services share a common multi-stage Dockerfile at `docker/Dockerfile`:

1. **Builder stage** — Uses `eclipse-temurin:17` to extract Spring Boot layered JAR via `java -Djarmode=layertools`.
2. **Runtime stage** — Uses `eclipse-temurin:17`, copies extracted layers (`dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`) for optimal Docker caching, and launches with `JarLauncher`.

The `docker-compose.yml` at the repository root orchestrates all 11 services, defines memory limits, health checks, port mappings, and the startup dependency chain described above.
