# Use JDK 23 runtime as a parent image
FROM openjdk:23-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the host to the container
COPY build/libs/ms-imgur-service-0.0.1-SNAPSHOT.jar app.jar

# Expose application and Redis ports
EXPOSE 9090 6379

# Install Redis in the container
RUN apt-get update && apt-get install -y redis && apt-get clean

# Run Redis in the background and start the Spring Boot application
ENTRYPOINT ["sh", "-c", "redis-server --daemonize yes && java -jar app.jar"]