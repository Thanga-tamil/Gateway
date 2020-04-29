# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Creating appuser and login as appuser
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
RUN chown -R appuser:appuser /var
USER appuser

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/gatewayservice.jar

# Add the application's jar to the container
ADD ${JAR_FILE} gatewayservice.jar

# Run the jar file
ENTRYPOINT exec java $JAVA_OPTS -jar /gatewayservice.jar
