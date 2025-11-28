# Start with a base image containing Java runtime
FROM eclipse-temurin:24-jre

RUN apt-get update
RUN apt-get install libgnutls30:amd64 systemd -y

RUN chmod 600 /etc/passwd && chmod 600 /etc/shadow && chmod 600 /etc/group

# Creating appuser and login as appuser
RUN addgroup   --system --gid 1001 appuser
RUN adduser  --system --uid  1001   --group appuser
RUN  chown -R appuser:appuser /opt
RUN  chown -R appuser:appuser /opt
USER appuser

WORKDIR /app
COPY --from=builder /build/target/gatewayservice.jar ./gatewayservice.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar gatewayservice.jar"]

