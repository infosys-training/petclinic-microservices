# Kubernetes Deployment for Spring PetClinic Microservices

This directory contains Kubernetes manifests to deploy the full Spring PetClinic Microservices stack.

## Prerequisites

- A running Kubernetes cluster (minikube, kind, EKS, GKE, AKS, etc.)
- `kubectl` configured to access your cluster
- Docker images built and available (see [Building Images](#building-images))

## Architecture

All resources are deployed into the `spring-petclinic` namespace:

| Service | Type | Port | K8s Service Type |
|---|---|---|---|
| Config Server | ClusterIP | 8888 | Internal |
| Discovery Server (Eureka) | ClusterIP | 8761 | Internal |
| Customers Service | ClusterIP | 8081 | Internal |
| Vets Service | ClusterIP | 8083 | Internal |
| Visits Service | ClusterIP | 8082 | Internal |
| GenAI Service | ClusterIP | 8084 | Internal |
| API Gateway | LoadBalancer | 8080 | External |
| Admin Server | ClusterIP | 9090 | Internal |
| Zipkin (Tracing) | ClusterIP | 9411 | Internal |
| Prometheus | ClusterIP | 9090 | Internal |
| Grafana | ClusterIP | 3000 | Internal |

## Quick Start

### 1. Build Docker Images

From the project root:

```bash
./mvnw clean install -P buildDocker
```

### 2. Deploy to Kubernetes

```bash
./k8s/deploy-all.sh
```

This script deploys services in the correct order, waiting for infrastructure services (Config Server, Discovery Server) to become healthy before deploying the application services.

### 3. Access the Application

```bash
kubectl port-forward svc/api-gateway 8080:8080 -n spring-petclinic
```

Then open http://localhost:8080 in your browser.

### 4. Tear Down

```bash
./k8s/destroy-all.sh
```

## Manual Deployment

If you prefer to deploy services individually:

```bash
# Namespace
kubectl apply -f k8s/namespace.yml

# Infrastructure (no dependencies)
kubectl apply -f k8s/tracing-server.yml
kubectl apply -f k8s/prometheus-server.yml
kubectl apply -f k8s/grafana-dashboards-configmap.yml
kubectl apply -f k8s/grafana-server.yml

# Config Server (must be ready before other Spring services)
kubectl apply -f k8s/config-server.yml
kubectl rollout status deployment/config-server -n spring-petclinic --timeout=120s

# Discovery Server (depends on Config Server)
kubectl apply -f k8s/discovery-server.yml
kubectl rollout status deployment/discovery-server -n spring-petclinic --timeout=120s

# Application services (depend on Config + Discovery)
kubectl apply -f k8s/customers-service.yml
kubectl apply -f k8s/vets-service.yml
kubectl apply -f k8s/visits-service.yml
kubectl apply -f k8s/genai-service.yml
kubectl apply -f k8s/api-gateway.yml
kubectl apply -f k8s/admin-server.yml
```

## Useful Port Forwards

```bash
# Eureka Dashboard
kubectl port-forward svc/discovery-server 8761:8761 -n spring-petclinic

# Spring Boot Admin
kubectl port-forward svc/admin-server 9090:9090 -n spring-petclinic

# Grafana Dashboards
kubectl port-forward svc/grafana-server 3000:3000 -n spring-petclinic

# Prometheus
kubectl port-forward svc/prometheus-server 9091:9090 -n spring-petclinic

# Zipkin Tracing
kubectl port-forward svc/tracing-server 9411:9411 -n spring-petclinic
```

## Configuration

### GenAI Service (OpenAI API Key)

The GenAI service defaults to the `demo` OpenAI API key. To use your own key, update the secret before deploying:

```bash
kubectl create secret generic genai-secrets \
  --from-literal=openai-api-key="your_api_key_here" \
  -n spring-petclinic \
  --dry-run=client -o yaml | kubectl apply -f -
```

For Azure OpenAI, add environment variables to the genai-service deployment:

```yaml
- name: AZURE_OPENAI_KEY
  valueFrom:
    secretKeyRef:
      name: genai-secrets
      key: azure-openai-key
- name: AZURE_OPENAI_ENDPOINT
  valueFrom:
    secretKeyRef:
      name: genai-secrets
      key: azure-openai-endpoint
```

### Resource Limits

Default resource settings:
- **Application services**: 256Mi-512Mi memory, 200m-500m CPU
- **Monitoring services**: 128Mi-256Mi memory, 100m-200m CPU

Adjust the `resources` section in each manifest according to your cluster capacity.

## Startup Order

Init containers ensure proper startup ordering:
1. **Config Server** starts first (no dependencies)
2. **Discovery Server** waits for Config Server health check
3. **All other services** wait for Discovery Server health check

## Monitoring

- **Prometheus** scrapes metrics from all Spring Boot services via `/actuator/prometheus`
- **Grafana** is pre-configured with a Prometheus datasource and a PetClinic dashboard
- **Zipkin** collects distributed traces from all services

## Scaling

To scale a service:

```bash
kubectl scale deployment/customers-service --replicas=3 -n spring-petclinic
```

Eureka will automatically register additional instances.
