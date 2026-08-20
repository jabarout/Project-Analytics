# Multi-stage production image for Project Analytics backend.
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && mv target/project-analytics-backend-*.jar target/application.jar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# curl is required for Docker HEALTHCHECK (not present on minimal JRE images).
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app \
    && mkdir -p /data/reports \
    && chown app:app /data/reports

COPY --from=build /workspace/target/application.jar /app/application.jar

USER app

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Image-level healthcheck used by Docker and compose (overridable).
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
