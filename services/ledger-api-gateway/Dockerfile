# ---------- STAGE 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# ---------- STAGE 2: Run ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/target/ledger-api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]