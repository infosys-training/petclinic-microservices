FROM prom/prometheus:v2.4.2
ADD prometheus.yml /etc/prometheus/

HEALTHCHECK --interval=15s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:9090/-/healthy || exit 1
