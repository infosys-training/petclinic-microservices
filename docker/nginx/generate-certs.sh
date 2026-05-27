#!/bin/sh
# Generates self-signed TLS certificates for local development.
# Certs are written to docker/nginx/certs/ and should NOT be committed
# to version control for production use.

CERT_DIR="$(dirname "$0")/certs"
mkdir -p "$CERT_DIR"

if [ -f "$CERT_DIR/server.crt" ] && [ -f "$CERT_DIR/server.key" ]; then
    echo "Certificates already exist in $CERT_DIR — skipping generation."
    exit 0
fi

openssl req -x509 -nodes -days 365 \
    -newkey rsa:2048 \
    -keyout "$CERT_DIR/server.key" \
    -out "$CERT_DIR/server.crt" \
    -subj "/C=US/ST=Local/L=Dev/O=PetClinic/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "Self-signed certificates generated in $CERT_DIR"
