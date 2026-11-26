# ===== STAGE 1: Builder (maven:3.9-eclipse-temurin-17) =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package

# ===== STAGE 2: Production Runtime (eclipse-temurin:17-jre-alpine) =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/app.jar /app/app.jar

RUN mkdir -p /app/data && chown -R app:app /app

USER app

ENV LINK_TTL=180 \
    FILE_LINK=./link.json \
    FILE_USER=./user.json

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
