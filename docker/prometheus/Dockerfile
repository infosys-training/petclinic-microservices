FROM prom/prometheus:v2.4.2
ADD prometheus.yml /etc/prometheus/

HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=5 \
    CMD wget --quiet --tries=1 --spider http://localhost:9090/-/healthy || exit 1
