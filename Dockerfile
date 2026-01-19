# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy source code
COPY src ./src

# Make the maven wrapper executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/sec-app-api-0.0.1-SNAPSHOT.jar"]
