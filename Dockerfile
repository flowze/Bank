# Stage 1: build with Maven + Eclipse Temurin 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: run on Eclipse Temurin 21 JRE (Debian Jammy)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/bank-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
