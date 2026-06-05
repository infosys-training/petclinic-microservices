# Docker Orchestration Report

## Architecture Overview

The PetClinic microservices are orchestrated via Docker Compose with an Nginx reverse proxy as a single entry point.

### Network Topology

```
                    ┌─────────────┐
                    │  nginx:80   │  (frontend + backend + monitoring)
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼──────┐
    │ frontend  │   │  backend  │   │ monitoring │
    └─────┬─────┘   └─────┬─────┘   └─────┬──────┘
          │                │                │
   api-gateway:8080  config-server    grafana:3000
                     discovery-server prometheus:9090
                     customers:8081
                     visits:8082
                     vets:8083
                     genai:8084
                     admin:9090
                     tracing:9411
```

## Accessing the Application via Reverse Proxy

All services are accessible through a single entry point at **port 80**:

| Route | Service | Direct Port |
|-------|---------|-------------|
| `http://localhost/` | API Gateway (main app UI) | 8080 |
| `http://localhost/eureka/` | Eureka Discovery Server | 8761 |
| `http://localhost/config/` | Config Server | 8888 |
| `http://localhost/admin/` | Spring Boot Admin | 9090 |
| `http://localhost/zipkin/` | Zipkin Tracing | 9411 |
| `http://localhost/grafana/` | Grafana Dashboards | 3030 |
| `http://localhost/prometheus/` | Prometheus Metrics | 9091 |
| `http://localhost/nginx-health` | Nginx Health Check | — |

## Starting the Stack

```bash
# Start all services (ordered by health dependencies)
docker compose up -d

# Start without the GenAI service (requires OPENAI_API_KEY)
docker compose up -d --scale genai-service=0
```

**Startup order (enforced via `depends_on` + `service_healthy`):**
1. Config Server → 2. Discovery Server → 3. Application services → 4. Nginx proxy

## Checking Health Status

```bash
# View all service statuses
docker compose ps

# Check individual service health
docker inspect --format='{{.State.Health.Status}}' <container-name>

# Check via actuator endpoints
curl http://localhost:8888/actuator/health   # Config Server
curl http://localhost:8761/actuator/health   # Discovery Server
curl http://localhost:8080/actuator/health   # API Gateway
curl http://localhost:8081/actuator/health   # Customers Service
curl http://localhost:8082/actuator/health   # Visits Service
curl http://localhost:8083/actuator/health   # Vets Service

# Nginx proxy health
curl http://localhost/nginx-health

# Eureka registered services
curl http://localhost:8761/eureka/apps
```

## Health Check Configuration

All Spring Boot services use `/actuator/health` with:
- `interval: 10s` — check every 10 seconds
- `timeout: 5s` — fail if no response in 5s
- `retries: 10` — allow up to 10 failures before marking unhealthy
- `start_period: 30-40s` — grace period for JVM startup

Third-party services:
- Zipkin: `wget` → `/health`
- Grafana: `curl` → `/api/health`
- Prometheus: `wget` → `/-/healthy`

## Notes

- The **GenAI service** requires `OPENAI_API_KEY` environment variable. Without it, the service will fail to start. Set it in a `.env` file or export it before running `docker compose up`.
- All services use `localhost` (not container names) in health check URLs to avoid DNS resolution failures during startup.
- The Nginx config uses Docker's embedded DNS resolver (`127.0.0.11`) with variable-based upstream resolution to handle services that may not be immediately available.
