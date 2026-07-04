# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy the source code and pom.xml
COPY . .

# Build the application and run tests
RUN mvn clean verify

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]