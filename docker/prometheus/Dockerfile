FROM prom/prometheus:v2.4.2
ADD prometheus.yml /etc/prometheus/

HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/-/healthy || exit 1
