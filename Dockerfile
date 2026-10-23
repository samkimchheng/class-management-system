# Build Stage
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies (caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create data and uploads directories
RUN mkdir -p /app/data /app/uploads

# Copy built application jar
COPY --from=builder /app/target/*.jar app.jar

# Copy existing database and uploads (audio files & existing data)
COPY data ./data
COPY uploads ./uploads

# Expose port
EXPOSE 8080
ENV PORT=8080

# Run Spring Boot
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
