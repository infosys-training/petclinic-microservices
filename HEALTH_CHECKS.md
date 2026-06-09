# Health Check Endpoints

This document describes the health check endpoints for all services in the Spring PetClinic Microservices application.

## Service Health Check Summary

| Service | Port | Health Endpoint | Actuator Enabled | Docker Health Check |
|---------|------|----------------|-----------------|-------------------|
| config-server | 8888 | `/actuator/health` | Yes | Yes |
| discovery-server | 8761 | `/actuator/health` | Yes | Yes |
| customers-service | 8081 | `/actuator/health` | Yes | Yes |
| visits-service | 8082 | `/actuator/health` | Yes | Yes |
| vets-service | 8083 | `/actuator/health` | Yes | Yes |
| genai-service | 8084 | `/actuator/health` | Yes | Yes |
| api-gateway | 8080 | `/actuator/health` | Yes | Yes |
| admin-server | 9090 | `/actuator/health` | Yes | Yes |
| tracing-server (Zipkin) | 9411 | `/health` | N/A | Yes |
| grafana-server | 3000 | `/api/health` | N/A | Yes |
| prometheus-server | 9090 | `/-/healthy` | N/A | Yes |

## Spring Boot Services

All Spring Boot services use [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) to expose health check endpoints.

### Configuration

Each service includes the following actuator configuration in its `application.yml`:

```yaml
management:
  endpoint:
    health:
      show-details: always
  endpoints:
    web:
      exposure:
        include: health,info
```

This exposes:
- `/actuator/health` — Returns service health status with detailed component information (disk space, database connectivity, Eureka registration status, etc.)
- `/actuator/info` — Returns application metadata

### Health Response Example

A healthy service returns HTTP 200 with a JSON response:

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 85899345920,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

Services connected to databases or Eureka will include additional component details.

## Non-Spring Services

### Zipkin (tracing-server)

- **Port:** 9411
- **Health Endpoint:** `/health`
- **Healthy Response:** HTTP 200 with `{"status":"UP"}`

### Grafana (grafana-server)

- **Port:** 3000 (mapped to host 3030)
- **Health Endpoint:** `/api/health`
- **Healthy Response:** HTTP 200 with `{"commit":"...","database":"ok","version":"..."}`

### Prometheus (prometheus-server)

- **Port:** 9090 (mapped to host 9091)
- **Health Endpoint:** `/-/healthy`
- **Healthy Response:** HTTP 200 with `Prometheus Server is Healthy.`

## Docker Health Checks

All services have Docker-level health checks configured in `docker-compose.yml`. The health checks use `curl` to probe each service's health endpoint.

### Spring Boot Services

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:<PORT>/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 15
  start_period: 40s
```

The `start_period` of 40 seconds accounts for JVM startup time. The health check runs every 10 seconds and allows up to 15 retries before marking the container as unhealthy.

### Non-Spring Services

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:<PORT>/<endpoint> || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 10
  start_period: 15s
```

Non-Spring services have a shorter `start_period` (15 seconds) since they typically start faster than JVM-based applications.

## Prerequisites

The base Docker image (`eclipse-temurin:17`) does not include `curl` by default. The `docker/Dockerfile` installs it:

```dockerfile
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
```

## Startup Order

Services depend on health checks for startup ordering via `condition: service_healthy`:

1. **config-server** — Starts first (no dependencies)
2. **discovery-server** — Waits for config-server to be healthy
3. **All application services** — Wait for both config-server and discovery-server to be healthy
4. **tracing-server, grafana-server, prometheus-server** — No startup dependencies
