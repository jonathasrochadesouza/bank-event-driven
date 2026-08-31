# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
# The Eclipse Temurin 21 JRE image already provides curl for the healthcheck.
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
# app.jar is relative to WORKDIR (/app/app.jar).
ENTRYPOINT ["java", "-jar", "app.jar"]
