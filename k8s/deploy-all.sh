#!/usr/bin/env bash
# Deploy all Spring Petclinic Microservices to Kubernetes.
# Usage: ./k8s/deploy-all.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Creating namespace..."
kubectl apply -f "$SCRIPT_DIR/namespace.yml"

echo "Deploying infrastructure services..."
kubectl apply -f "$SCRIPT_DIR/tracing-server.yml"
kubectl apply -f "$SCRIPT_DIR/prometheus-server.yml"
kubectl apply -f "$SCRIPT_DIR/grafana-dashboards-configmap.yml"
kubectl apply -f "$SCRIPT_DIR/grafana-server.yml"

echo "Deploying Config Server..."
kubectl apply -f "$SCRIPT_DIR/config-server.yml"
echo "Waiting for Config Server to be ready..."
kubectl rollout status deployment/config-server -n spring-petclinic --timeout=120s

echo "Deploying Discovery Server..."
kubectl apply -f "$SCRIPT_DIR/discovery-server.yml"
echo "Waiting for Discovery Server to be ready..."
kubectl rollout status deployment/discovery-server -n spring-petclinic --timeout=120s

echo "Deploying application services..."
kubectl apply -f "$SCRIPT_DIR/customers-service.yml"
kubectl apply -f "$SCRIPT_DIR/vets-service.yml"
kubectl apply -f "$SCRIPT_DIR/visits-service.yml"
kubectl apply -f "$SCRIPT_DIR/genai-service.yml"
kubectl apply -f "$SCRIPT_DIR/api-gateway.yml"
kubectl apply -f "$SCRIPT_DIR/admin-server.yml"

echo ""
echo "All services deployed. Waiting for rollouts to complete..."
for svc in customers-service vets-service visits-service genai-service api-gateway admin-server; do
  echo "  Waiting for $svc..."
  kubectl rollout status deployment/$svc -n spring-petclinic --timeout=180s
done

echo ""
echo "All services are running!"
echo ""
echo "Access the application:"
echo "  kubectl port-forward svc/api-gateway 8080:8080 -n spring-petclinic"
echo "  Then open: http://localhost:8080"
echo ""
echo "Other useful port-forwards:"
echo "  Eureka Dashboard:  kubectl port-forward svc/discovery-server 8761:8761 -n spring-petclinic"
echo "  Admin Server:      kubectl port-forward svc/admin-server 9090:9090 -n spring-petclinic"
echo "  Grafana:           kubectl port-forward svc/grafana-server 3000:3000 -n spring-petclinic"
echo "  Prometheus:        kubectl port-forward svc/prometheus-server 9091:9090 -n spring-petclinic"
echo "  Zipkin:            kubectl port-forward svc/tracing-server 9411:9411 -n spring-petclinic"
