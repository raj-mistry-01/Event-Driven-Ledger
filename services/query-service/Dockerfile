# ---------- STAGE 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first (for caching dependencies)
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ---------- STAGE 2: Run ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/query-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port (your service runs on 8082)
EXPOSE 8094

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]