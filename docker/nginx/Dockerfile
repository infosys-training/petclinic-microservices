FROM nginx:1.25-alpine

RUN apk add --no-cache curl

COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost/nginx-health || exit 1
