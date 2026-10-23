# Build Stage
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Limit Maven memory to fit Render 512MB free tier
ENV MAVEN_OPTS="-Xmx384m"

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build application jar (skip tests and batch mode)
RUN mvn clean package -DskipTests -B

# Runtime Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create directories for database and uploads
RUN mkdir -p /app/data /app/uploads

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Copy audio files and uploads
COPY uploads ./uploads

# Port configuration
EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS="-Xmx384m"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
