FROM maven:3.9.16-eclipse-temurin-21 AS builder
LABEL authors="la-chaise-du-savoir"
COPY . /app
WORKDIR /app
RUN mvn package

FROM eclipse-temurin:21-jre-ubi10-minimal
COPY --from=builder /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]

