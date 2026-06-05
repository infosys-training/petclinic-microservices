FROM prom/prometheus:v2.4.2
ADD prometheus.yml /etc/prometheus/

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=15s \
  CMD wget --spider -q http://localhost:9090/-/healthy || exit 1
