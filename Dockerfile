# syntax=docker/dockerfile:1

FROM node:22-alpine AS frontend
WORKDIR /src
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /src
COPY .mvn ./.mvn
COPY mvnw mvnw.cmd pom.xml ./
COPY src ./src
COPY --from=frontend /src/dist/bench/browser ./frontend/dist/bench/browser
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S bench && adduser -S -G bench bench
WORKDIR /app
COPY --from=backend /src/target/bench-0.1.0.jar app.jar
RUN mkdir -p /app/data && chown -R bench:bench /app
USER bench

ENV PORT=8080 \
    BENCH_DATA_FILE=/app/data/site.json \
    SITE_URL=https://maxmya.com

VOLUME ["/app/data"]
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
