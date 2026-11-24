# ========= Stage 1: build =========
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала только pom — кэш слоёв Maven при неизменных зависимостях
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Потом исходники и сборка fat-jar (тесты прогоняются также в CI-джобе build)
COPY src ./src
RUN mvn -B -q clean package

# ========= Stage 2: runtime =========
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root пользователь
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/app.jar /app/app.jar
RUN mkdir -p /app/data && chown -R app:app /app
USER app

# значения по умолчанию
ENV LINK_TTL=180 \
    FILE_LINK=./link.json \
    FILE_USER=./user.json

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
