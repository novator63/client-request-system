FROM maven:3.9.10-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Cache dependencies first for faster rebuilds.
COPY app/pom.xml app/pom.xml
RUN mvn -f app/pom.xml -B -DskipTests dependency:go-offline

COPY app app
RUN mvn -f app/pom.xml -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
