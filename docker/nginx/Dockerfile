FROM nginx:1.25-alpine

RUN apk add --no-cache curl

COPY nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=10s --timeout=5s --start_period=10s --retries=3 \
  CMD curl -f http://localhost:80/nginx-health || exit 1

EXPOSE 80
