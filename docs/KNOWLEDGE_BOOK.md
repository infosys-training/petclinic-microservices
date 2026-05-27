# Spring PetClinic Microservices — Knowledge Book

> A comprehensive guide to the architecture, design patterns, and best practices implemented in the Spring PetClinic Microservices project.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Microservice Decomposition](#2-microservice-decomposition)
3. [Service Discovery (Eureka)](#3-service-discovery-eureka)
4. [Centralized Configuration (Config Server)](#4-centralized-configuration-config-server)
5. [API Gateway](#5-api-gateway)
6. [Inter-Service Communication](#6-inter-service-communication)
7. [Resilience & Fault Tolerance](#7-resilience--fault-tolerance)
8. [Observability Stack](#8-observability-stack)
9. [Data Management](#9-data-management)
10. [Containerization & Docker](#10-containerization--docker)
11. [Generative AI Integration (Spring AI)](#11-generative-ai-integration-spring-ai)
12. [Chaos Engineering](#12-chaos-engineering)
13. [Testing Strategy](#13-testing-strategy)
14. [Code Quality & Design Patterns](#14-code-quality--design-patterns)
15. [Security Considerations](#15-security-considerations)
16. [Build & Dependency Management](#16-build--dependency-management)
17. [Operational Readiness](#17-operational-readiness)

---

## 1. Architecture Overview

The application follows a **distributed microservices architecture** built on the Spring Cloud ecosystem. Each business domain is encapsulated in an independently deployable service, coordinated through infrastructure services for discovery, configuration, routing, and monitoring.

**Technology Stack:**
- **Runtime:** Java 17 on Eclipse Temurin
- **Framework:** Spring Boot 4.0.1
- **Cloud:** Spring Cloud 2025.1.0 (Eureka, Config, Gateway)
- **AI:** Spring AI 2.0.0-M1
- **Database:** HSQLDB (in-memory, default) / MySQL (persistent, via profile)
- **Monitoring:** Prometheus, Grafana, Zipkin, Spring Boot Admin

**Services:**

| Service | Role | Default Port |
|---|---|---|
| Config Server | Centralized configuration | 8888 |
| Discovery Server | Eureka service registry | 8761 |
| API Gateway | Edge routing, UI hosting, circuit breaking | 8080 |
| Customers Service | Owner & pet management | Random (8081 in Docker) |
| Vets Service | Veterinarian data & specialties | Random (8083 in Docker) |
| Visits Service | Appointment records | Random (8082 in Docker) |
| GenAI Service | LLM-powered chatbot | Random (8084 in Docker) |
| Admin Server | Spring Boot Admin monitoring UI | 9090 |

---

## 2. Microservice Decomposition

### Best Practices Demonstrated

**Single Responsibility Principle**
Each microservice owns a single bounded context:
- `customers-service` → Owners, Pets, PetTypes
- `vets-service` → Veterinarians, Specialties
- `visits-service` → Visit records

**Independent Deployability**
- Each service is a self-contained Spring Boot application with its own `pom.xml`, database schema, and application configuration.
- Services start on random ports (in non-Docker mode) and register themselves with Eureka, enabling horizontal scaling.

**Consistent Project Structure**
Every business service follows a layered architecture:
```
src/main/java/.../
├── <ServiceName>Application.java   # @SpringBootApplication entry point
├── config/                          # Configuration beans (MetricConfig, CacheConfig)
├── model/                           # JPA entities + Spring Data repositories
└── web/                             # REST controllers, DTOs, mappers
```

**Shared Parent POM**
All modules inherit from a common parent POM (`spring-petclinic-microservices`) that manages:
- Spring Boot parent version
- Spring Cloud BOM via `dependencyManagement`
- Common plugin configuration (build-info, git-commit-id, enforcer)
- Docker build profile

---

## 3. Service Discovery (Eureka)

### Pattern: Client-Side Service Discovery

**Server Configuration** (`spring-petclinic-discovery-server`):
- Annotated with `@EnableEurekaServer`
- Runs in standalone mode (`registerWithEureka: false`, `fetchRegistry: false`)
- Fixed port `8761`

**Client Registration** (all business services):
- Annotated with `@EnableDiscoveryClient`
- Each instance registers with a unique `instance-id` using `${spring.application.name}:${random.uuid}` for multi-instance support
- `prefer-ip-address: true` avoids DNS resolution failures across environments

**Best Practices:**
- Services use logical service names (e.g., `customers-service`) instead of hardcoded host/port, enabling load balancing and failover automatically.
- The discovery server is a prerequisite; all other services depend on it via Docker Compose `service_healthy` conditions.

---

## 4. Centralized Configuration (Config Server)

### Pattern: Externalized Configuration

**Server** (`spring-petclinic-config-server`):
- Annotated with `@EnableConfigServer`
- Supports two backends:
  - **Git** (default): Pulls from `https://github.com/spring-petclinic/spring-petclinic-microservices-config`
  - **Native/File System**: Activated via `native` profile with `GIT_REPO` env variable pointing to a local clone

**Client Configuration** (all services):
```yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
```

**Best Practices:**
- The `optional:` prefix ensures services can start without the config server during development.
- Environment-specific overrides use Spring profile-based YAML documents (e.g., `on-profile: docker`).
- The config repo uses `allow-override: true` and `override-none: true` so local properties take precedence, enabling per-service customization.
- Common properties (server settings, JPA, actuator, metrics, tracing, Eureka) are defined once in `application.yml` of the config repo and inherited by all services.
- Service-specific properties are in dedicated files (e.g., `customers-service.yml`, `vets-service.yml`).

---

## 5. API Gateway

### Pattern: Edge Service / Backend for Frontend

**Implementation:** Spring Cloud Gateway (WebFlux-based, `spring-cloud-starter-gateway-server-webflux`)

**Routing Configuration:**
```yaml
routes:
  - id: vets-service
    uri: lb://vets-service          # Load-balanced via Eureka
    predicates:
      - Path=/api/vet/**
    filters:
      - StripPrefix=2               # Removes /api/vet prefix
```

**Best Practices:**
- **Load Balancing:** Routes use `lb://` scheme for client-side load balancing through Eureka.
- **Path-Based Routing:** Clean URL namespacing (`/api/vet/**`, `/api/visit/**`, `/api/customer/**`, `/api/genai/**`).
- **Prefix Stripping:** `StripPrefix=2` removes gateway path prefixes before forwarding to downstream services.
- **Static Content Hosting:** The gateway serves the AngularJS frontend via WebJars and a custom `RouterFunction` for SPA routing.
- **Response Compression:** Enabled for JSON, CSS, and JavaScript with a 2KB minimum threshold.
- **Reactive Context Propagation:** `spring.reactor.context-propagation: auto` ensures trace context flows through reactive pipelines.

---

## 6. Inter-Service Communication

### Patterns Used

**Reactive WebClient (Gateway → Services):**
```java
@Bean
@LoadBalanced
public WebClient.Builder loadBalancedWebClientBuilder() {
    return WebClient.builder();
}
```
- `@LoadBalanced` WebClient integrates with Eureka for service resolution.
- Used in `CustomersServiceClient` and `VisitsServiceClient` for non-blocking calls.

**RestClient (GenAI → Services):**
- `AIDataProvider` uses `RestClient` with `DiscoveryClient` for programmatic service instance resolution.
- Resolves service URIs dynamically: `discoveryClient.getInstances("customers-service").get(0).getUri()`

**Data Aggregation (API Composition Pattern):**
The `ApiGatewayController` composes data from multiple services:
1. Fetches owner details from `customers-service`
2. Fetches visits from `visits-service`
3. Merges visits into owner's pet records

This avoids distributed joins and keeps each service's data model independent.

---

## 7. Resilience & Fault Tolerance

### Circuit Breaker (Resilience4j)

**Gateway-Level Configuration:**
```yaml
default-filters:
  - name: CircuitBreaker
    args:
      name: defaultCircuitBreaker
      fallbackUri: forward:/fallback
  - name: Retry
    args:
      retries: 1
      statuses: SERVICE_UNAVAILABLE
      methods: POST
```

**Application-Level Circuit Breaker:**
```java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        .timeLimiterConfig(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10)).build())
        .build());
}
```

**Best Practices:**
- **Fallback Controller:** A dedicated `FallbackController` returns HTTP 503 with a user-friendly message when downstream services are unavailable.
- **Graceful Degradation:** In `ApiGatewayController`, if the visits service fails, the circuit breaker returns an empty visits list instead of propagating the error — the owner details are still returned.
- **Per-Service Circuit Breakers:** The GenAI service route has its own named circuit breaker (`genaiCircuitBreaker`) for independent fault isolation.
- **Time Limiter:** 10-second timeout prevents long-running requests from consuming resources.
- **Retry Policy:** POST requests are retried once on `SERVICE_UNAVAILABLE`, enabling recovery from transient failures.

### Graceful Shutdown
```yaml
server:
  shutdown: graceful
```
Allows in-flight requests to complete before the service shuts down.

---

## 8. Observability Stack

### Three Pillars Implementation

**1. Metrics (Micrometer + Prometheus + Grafana)**

- **Micrometer** is the metrics facade, with `micrometer-registry-prometheus` exporting in Prometheus format.
- **Custom Business Metrics** via `@Timed` annotation on REST controllers:
  - `petclinic.owner` on `OwnerResource`
  - `petclinic.pet` on `PetResource`
  - `petclinic.visit` on `VisitResource`
- **MetricConfig** registers common tags (`application=petclinic`) and enables `TimedAspect` for annotation-based timing.
- **Prometheus** scrapes `/actuator/prometheus` endpoints from each service at 15-second intervals.
- **Grafana** ships with a pre-built dashboard (`grafana-petclinic-dashboard.json`) and auto-provisioned Prometheus data source.

**2. Distributed Tracing (Micrometer Tracing + Zipkin)**

- Uses `micrometer-tracing-bridge-brave` and `opentelemetry-exporter-zipkin`.
- 100% sampling rate (`management.tracing.sampling.probability: 1`) in all environments.
- Trace context propagates across service boundaries via HTTP headers.
- `datasource-micrometer-spring-boot` adds JDBC query tracing.
- Zipkin endpoint configured per profile (Docker: `http://tracing-server:9411/api/v2/spans`).

**3. Health & Administration (Spring Boot Actuator + Spring Boot Admin)**

- **All actuator endpoints exposed:** `management.endpoints.web.exposure.include: "*"`
- **Build Info:** `spring-boot-maven-plugin` generates `META-INF/build-info.properties` displayed in actuator.
- **Git Info:** `git-commit-id-maven-plugin` generates `git.properties` for commit tracking.
- **Spring Boot Admin Server** (`@EnableAdminServer`) discovers services via Eureka and provides a unified monitoring UI at port 9090.

---

## 9. Data Management

### Database Strategy

**Dual-Profile Database Support:**
- **Default (HSQLDB):** In-memory database for development and testing. Schema and data auto-initialized from `db/hsqldb/schema.sql` and `db/hsqldb/data.sql`.
- **MySQL Profile:** Persistent storage activated with `--spring.profiles.active=mysql`. Separate schema/data scripts in `db/mysql/`.

**Best Practices:**
- **Database per Service:** Each data-owning service (customers, vets, visits) has its own schema, enforcing data isolation.
- **JPA Configuration:**
  - `open-in-view: false` — Prevents lazy loading anti-pattern in the view layer.
  - `ddl-auto: none` — Schema managed by explicit SQL scripts, not auto-generated.
- **Spring Data JPA Repositories:** Clean interfaces with method-name-based queries:
  ```java
  List<Visit> findByPetIdIn(Collection<Integer> petIds);
  ```
- **Entity Design:**
  - Uses Jakarta Persistence annotations with explicit `@Table` and `@Column` mappings.
  - `@JsonIgnore` on bidirectional relationships (e.g., `Pet.owner`) to prevent infinite recursion in JSON serialization.
  - Sorted collections via `PropertyComparator` for deterministic ordering.
  - Validation constraints (`@NotBlank`, `@Digits`, `@Size`) directly on entity fields.

### Caching

The vets service implements caching via `@Cacheable("vets")` on the `showResourcesVetList()` endpoint:
- Cache is enabled only in the `production` profile (`@Profile("production")`) via `CacheConfig`, allowing tests to run without cache interference.
- Cache TTL and heap size are externalized via `VetsProperties` record using `@ConfigurationProperties(prefix = "vets")`:
  ```java
  public record VetsProperties(Cache cache) {
      public record Cache(int ttl, int heapSize) {}
  }
  ```
- Default: 60-second TTL, 100-entry heap size (configured in `vets-service.yml`).

---

## 10. Containerization & Docker

### Multi-Stage Layered Dockerfile

```dockerfile
FROM eclipse-temurin:17 AS builder
WORKDIR application
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17
WORKDIR application
ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

**Best Practices:**
- **Layered JARs:** Spring Boot's `layertools` extracts the JAR into four layers (dependencies, spring-boot-loader, snapshot-dependencies, application). Unchanged layers are cached by Docker, dramatically reducing rebuild and push times.
- **Shared Dockerfile:** A single Dockerfile in `docker/` is reused across all services via parameterized `ARG` values (`ARTIFACT_NAME`, `EXPOSED_PORT`).
- **Optimized Entrypoint:** Uses `JarLauncher` instead of `java -jar` for proper classloader isolation.
- **Profile Activation:** `SPRING_PROFILES_ACTIVE=docker` automatically activates Docker-specific config (service hostnames, Zipkin endpoint).
- **Multi-Platform Support:** Build args support `linux/amd64` (default) and `linux/arm64` via `-Dcontainer.platform`.
- **Podman Compatibility:** Container executable is configurable via `-Dcontainer.executable=podman`.

### Docker Compose Orchestration

**Best Practices:**
- **Health-Based Dependency Ordering:** `depends_on` with `condition: service_healthy` ensures Config Server and Discovery Server are ready before business services start.
- **Health Checks:** Config Server uses `curl -I http://config-server:8888`; Discovery Server uses `curl -f http://discovery-server:8761`.
- **Resource Limits:** Each service is capped at 512MB memory; monitoring services at 256MB.
- **Environment Variable Injection:** Sensitive values (API keys) passed via environment variables, not baked into images.

---

## 11. Generative AI Integration (Spring AI)

### Architecture

The `genai-service` integrates LLM capabilities using **Spring AI**:

**Chat Client Configuration:**
```java
this.chatClient = builder
    .defaultSystem("You are a friendly AI assistant...")
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).order(10).build(),
        new SimpleLoggerAdvisor()
    )
    .defaultTools(petclinicTools)
    .build();
```

**Best Practices:**
- **System Prompt Engineering:** A detailed system prompt defines the AI's role, tone, and boundaries.
- **Tool Calling (Function Calling):** `PetclinicTools` exposes annotated methods (`@Tool`) that the LLM can invoke:
  - `listOwners()` — Lists all owners
  - `addOwnerToPetclinic()` — Creates new owners
  - `listVets()` — Searches vets via vector similarity
  - `addPetToOwner()` — Adds pets to owners
- **RAG (Retrieval Augmented Generation):** Vet data is loaded into a `SimpleVectorStore` at startup for semantic search via `VectorStoreController`.
- **Chat Memory:** `MessageChatMemoryAdvisor` maintains conversation context for up to 10 previous messages.
- **Provider Abstraction:** Supports both OpenAI and Azure OpenAI by swapping the starter dependency. API keys are externalized as environment variables.
- **Graceful Error Handling:** Chat endpoint catches exceptions and returns a user-friendly fallback message.
- **Cost Optimization:** Pre-embedded vector store data (`vectorstore.json`) is loaded from classpath to avoid repeated embedding API calls.

---

## 12. Chaos Engineering

### Chaos Monkey for Spring Boot

Integrated via `de.codecentric:chaos-monkey-spring-boot`:

**Configuration (in config repo `application.yml`):**
```yaml
spring:
  config:
    activate:
      on-profile: chaos-monkey
chaos:
  monkey:
    enabled: true
    watcher:
      component: false
      controller: false
      repository: false
      rest-controller: false
      service: false
```

**Best Practices:**
- **Profile-Gated:** Chaos Monkey is only active when `chaos-monkey` profile is explicitly activated.
- **Watchers Off By Default:** All watchers start disabled; operators selectively enable them at runtime via Actuator endpoints.
- **Helper Scripts:** `scripts/chaos/call_chaos.sh` provides a CLI for targeting specific services and enabling attack types:
  - **Attacks:** Exception injection, latency, memory stress, application kill
  - **Watchers:** Component, controller, repository, REST controller, service layers
- **Runtime Configuration:** JSON payloads in `scripts/chaos/` can be applied via Actuator endpoints without restart.

---

## 13. Testing Strategy

### Test Types Implemented

**1. Web Layer Tests (Slice Tests)**
- `@WebMvcTest` for servlet-based controllers (`PetResourceTest`, `VetResourceTest`)
- `@WebFluxTest` for reactive controllers (`ApiGatewayControllerTest`)
- Use `@MockitoBean` for dependency isolation
- Use `@ActiveProfiles("test")` to disable Config Server and Eureka clients

**2. Integration Tests**
- `VisitsServiceClientIntegrationTest` uses `MockWebServer` (OkHttp) to test `WebClient`-based HTTP calls against a local mock server
- Tests verify JSON serialization/deserialization contracts

**3. Circuit Breaker Tests**
- `ApiGatewayControllerTest.getOwnerDetails_withServiceError()` verifies that Resilience4j fallback returns empty visits when the visits service throws `ConnectException`

**Best Practices:**
- **Test Profile Configuration:** `application-test.yml` disables cloud infrastructure (`spring.cloud.config.enabled: false`, `eureka.client.enabled: false`) so tests run independently.
- **Builder Pattern for Test Data:** DTOs use static builder classes (e.g., `OwnerDetailsBuilder`, `PetDetailsBuilder`, `VisitBuilder`) for clean test data construction.
- **Assertion Libraries:** Uses AssertJ and JUnit Jupiter for modern, fluent assertions.
- **Contract Testing via MockMvc:** Tests verify JSON path expectations for API contracts.

---

## 14. Code Quality & Design Patterns

### Patterns & Conventions

**1. Records for DTOs**
Immutable data transfer objects use Java records:
```java
public record OwnerRequest(@NotBlank String firstName,
                           @NotBlank String lastName, ...)  {}
```
Records enforce immutability, reduce boilerplate, and support validation annotations.

**2. Builder Pattern**
Complex DTOs provide static inner builders for test and construction convenience:
```java
public static final class OwnerDetailsBuilder {
    public static OwnerDetailsBuilder anOwnerDetails() { ... }
    public OwnerDetailsBuilder pets(List<PetDetails> pets) { ... }
    public OwnerDetails build() { ... }
}
```

**3. Mapper Interface**
A generic `Mapper<R, E>` interface decouples request-to-entity mapping:
```java
public interface Mapper<R, E> {
    E map(E response, R request);
}
```
Implemented by `OwnerEntityMapper` for `OwnerRequest → Owner` conversion.

**4. Constructor Injection**
All Spring beans use constructor injection (no `@Autowired` on fields), enabling:
- Immutable dependencies
- Easier unit testing
- Explicit dependency declaration

**5. Package-Private Visibility**
Controllers and internal classes use package-private access (`class VetResource`) rather than `public`, enforcing encapsulation.

**6. Custom Exception Handling**
`ResourceNotFoundException` provides clean 404 responses when entities are not found:
```java
ownerRepository.findById(ownerId)
    .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
```

**7. Typesafe Configuration Properties**
`VetsProperties` record with `@ConfigurationProperties` provides compile-time-safe, IDE-friendly access to external configuration.

---

## 15. Security Considerations

### Best Practices Observed

- **No Credentials in Code:** Database credentials, API keys, and service endpoints are externalized via environment variables and Spring Cloud Config.
- **POSIX File Permissions:** Vector store temp files are created with `rwx------` permissions, preventing unauthorized access (Sonar rule `java:S5443` compliance).
- **Validation at Boundaries:** Jakarta Validation constraints (`@NotBlank`, `@Digits`, `@Size`, `@Min`) on request DTOs and path variables prevent malformed input.
- **JSON Serialization Safety:** `@JsonIgnore` on bidirectional JPA relationships prevents data leakage and infinite recursion.
- **Profile-Based Security:** Chaos Monkey and anonymous Grafana access are gated behind specific profiles, preventing accidental exposure in production.

### Areas for Production Hardening
- Actuator endpoints (`management.endpoints.web.exposure.include: "*"`) should be restricted in production.
- API Gateway routes lack authentication/authorization filters — consider adding Spring Security with OAuth2.
- Grafana anonymous admin access is explicitly noted as development-only in the configuration.

---

## 16. Build & Dependency Management

### Maven Multi-Module Project

**Parent POM Best Practices:**
- **Spring Boot Parent:** Inherits dependency management, plugin configuration, and property defaults from `spring-boot-starter-parent`.
- **Spring Cloud BOM:** Imported as a `pom` scope dependency in `dependencyManagement` for consistent cloud dependency versions.
- **Version Enforcement:** `maven-enforcer-plugin` validates Java 17+ is available at build time with a descriptive error message.
- **Git Metadata:** `git-commit-id-maven-plugin` generates `git.properties` for build traceability.
- **Build Info:** `spring-boot-maven-plugin` generates `META-INF/build-info.properties` for Actuator `/info` endpoint.
- **Profile-Based Docker Builds:** The `buildDocker` profile uses `exec-maven-plugin` to invoke `docker build` with parameterized arguments.

**Dependency Organization:**
Dependencies in each child POM are organized into clear sections:
1. Spring Boot starters
2. Spring Cloud starters
3. Third-party libraries (monitoring, database drivers, chaos monkey)
4. Testing dependencies

---

## 17. Operational Readiness

### Deployment Patterns

**Local Development:**
- Start Config Server and Discovery Server first, then business services.
- Services auto-register via Eureka; API Gateway syncs routes dynamically.

**Docker Compose:**
- Health-check-based startup ordering eliminates race conditions.
- Infrastructure services (Grafana, Prometheus, Zipkin) run alongside application services.
- `run_all.sh` script provides a hybrid mode: Docker for infrastructure, native Java for app services.

**Production Readiness Checklist:**
- [ ] Restrict actuator endpoint exposure
- [ ] Configure MySQL profile for persistent storage
- [ ] Secure Grafana with proper authentication
- [ ] Add rate limiting at the API Gateway
- [ ] Configure circuit breaker thresholds per service SLA
- [ ] Set up TLS termination at the gateway
- [ ] Implement centralized logging (ELK/Loki)
- [ ] Add authentication/authorization via Spring Security
- [ ] Configure auto-scaling based on Prometheus metrics
- [ ] Set up alerting rules in Grafana

---

## Summary of Key Best Practices

| Category | Best Practice | Implementation |
|---|---|---|
| **Architecture** | Database per service | Separate schema/data per service module |
| **Architecture** | API Composition | Gateway aggregates owner + visits data |
| **Configuration** | Externalized config | Spring Cloud Config with Git/native backends |
| **Configuration** | Profile-based overrides | `docker`, `mysql`, `chaos-monkey`, `production` profiles |
| **Discovery** | Client-side discovery | Eureka with `@EnableDiscoveryClient` |
| **Resilience** | Circuit breaker | Resilience4j with fallback methods |
| **Resilience** | Graceful degradation | Empty visits on service failure |
| **Resilience** | Graceful shutdown | `server.shutdown: graceful` |
| **Observability** | Metrics | Micrometer + Prometheus + Grafana dashboards |
| **Observability** | Distributed tracing | Brave/Zipkin with 100% sampling |
| **Observability** | Health monitoring | Actuator + Spring Boot Admin |
| **Containerization** | Layered Docker images | Spring Boot layertools for optimal caching |
| **Containerization** | Health-based orchestration | Docker Compose `service_healthy` |
| **Testing** | Slice tests | `@WebMvcTest` / `@WebFluxTest` with mocks |
| **Testing** | Test isolation | Test profile disables cloud infrastructure |
| **Code Quality** | Immutable DTOs | Java records with validation |
| **Code Quality** | Constructor injection | No field injection anywhere |
| **Code Quality** | Encapsulation | Package-private controllers |
| **AI Integration** | Tool calling | `@Tool` annotated methods for LLM actions |
| **AI Integration** | RAG | Vector store for semantic vet search |
| **Chaos** | Resilience testing | Profile-gated Chaos Monkey |
