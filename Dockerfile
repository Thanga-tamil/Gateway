FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src

# Use --no-transfer-progress to cut network chatter and size
RUN mvn -B clean package -DskipTests


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
# alpine is ~80 MB smaller than slim-bullseye

# Add system dependencies only if your app truly needs them
# libgnutls is usually not required for most Java apps; include only if necessary
RUN addgroup -S appuser && adduser -S appuser -G appuser

USER appuser

WORKDIR /app
COPY --from=builder /build/target/gatewayservice.jar ./gatewayservice.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar gatewayservice.jar"]