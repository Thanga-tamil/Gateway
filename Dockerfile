# Start with a base image containing Java runtime
FROM openjdk:8-jre-slim

RUN apt-get update
RUN apt-get install libgnutls30:amd64 systemd -y

RUN chmod 600 /etc/passwd && chmod 600 /etc/shadow && chmod 600 /etc/group

# Creating appuser and login as appuser
RUN addgroup   --system --gid 1001 appuser
RUN adduser  --system --uid  1001   --group appuser
RUN  chown -R appuser:appuser /opt
RUN  chown -R appuser:appuser /opt
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