FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy only pom first for faster dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime

# Create a non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser

USER appuser

WORKDIR /app

# Copy compiled JAR from builder stage
COPY --from=builder /build/target/gatewayservice.jar ./gatewayservice.jar

# Expose application port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar gatewayservice.jar"]

