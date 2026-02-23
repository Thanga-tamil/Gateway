# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests --no-transfer-progress

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S appuser && adduser -S appuser -G appuser && chown -R appuser:appuser /opt

USER appuser

WORKDIR /app
COPY --from=builder /build/target/gatewayservice.jar ./gatewayservice.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gatewayservice.jar"]


