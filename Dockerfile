# Stage 1: Build the project using Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy all project files to container
COPY . .

# Build the Spring Boot application, skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Run the app with lightweight JDK 21 image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the built jar from the build stage; adjust jar name if needed
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 for the app
EXPOSE 8080

# Run the Spring Boot app
CMD ["java", "-jar", "app.jar"]
