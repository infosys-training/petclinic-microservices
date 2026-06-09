FROM grafana/grafana:5.2.4
ADD ./provisioning /etc/grafana/provisioning
ADD ./grafana.ini /etc/grafana/grafana.ini
ADD ./dashboards /var/lib/grafana/dashboards

HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=5 \
    CMD wget --quiet --tries=1 --spider http://localhost:3000/api/health || exit 1
