# Stage 1: Build the application using Maven with JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies to leverage Docker's layer caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image with JRE 17
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on (from application.yml)
EXPOSE 8085

# Add a health check to monitor the application's status
# The service runs on context-path /scheduling-service, so the health endpoint is at /scheduling-service/actuator/health
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s \
  CMD curl -f http://localhost:8085/scheduling-service/actuator/health || exit 1

# Set the entrypoint to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
