# =============================================================================
# Multi-stage Dockerfile for Spring PetClinic Microservices
# =============================================================================
# Shared by all Spring Boot services (customers, visits, vets, genai,
# api-gateway, admin-server, config-server, discovery-server).
#
# Stage 1 (builder): Extracts Spring Boot layered JAR into separate directories
#   for optimized Docker layer caching.
# Stage 2 (runtime): Minimal JRE image with curl installed for health checks.
# =============================================================================

# --- Stage 1: Extract Spring Boot layers from the fat JAR ---
FROM eclipse-temurin:17 AS builder
WORKDIR application
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


# --- Stage 2: Runtime image ---
FROM eclipse-temurin:17

# Install curl for Docker health check commands (used in docker-compose.yml
# healthcheck directives to probe /actuator/health endpoints)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR application

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

ENV SPRING_PROFILES_ACTIVE=docker

# Copy extracted layers in order of least-to-most frequently changing
# for optimal Docker build cache utilization
COPY --from=builder application/dependencies/ ./

# Workaround for Docker export failures on btrfs filesystems
# See: https://stackoverflow.com/questions/51115856
# See: https://github.com/moby/moby/issues/36573
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
