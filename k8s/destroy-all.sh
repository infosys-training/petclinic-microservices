#!/usr/bin/env bash
# Remove all Spring Petclinic Microservices from Kubernetes.
# Usage: ./k8s/destroy-all.sh

set -euo pipefail

echo "Deleting spring-petclinic namespace and all resources..."
kubectl delete namespace spring-petclinic --ignore-not-found=true
echo "Done."
