
# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

RUN mkdir -pv /opt/files

# Add a volume pointing to /tmp
VOLUME /tmp


# Make port 8080 available to the world outside this container
EXPOSE 9090

# The application's jar file
ARG JAR_FILE=target/gatewayservice.jar

# Add the application's jar to the container
ADD ${JAR_FILE} gatewayservice.jar

# Run the jar file 
ENTRYPOINT exec java $JAVA_OPTS -jar /gatewayservice.jar
