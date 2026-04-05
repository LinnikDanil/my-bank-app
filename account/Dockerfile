# ─────────────────────────────────────────────────────────────
# ЭТАП 1: Сборка сервиса account внутри Docker
# ─────────────────────────────────────────────────────────────
FROM gradle:9.3.1-jdk25 AS builder
WORKDIR /app

# Копируем Gradle wrapper и build-файлы только тех модулей, которые нужны для сборки account
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN mkdir -p common account cash front notification transfer
COPY common/build.gradle common/build.gradle
COPY account/build.gradle account/build.gradle

# Копируем исходники только тех модулей, которые реально нужны для сборки account
COPY common/src common/src
COPY account/src account/src
COPY openapi openapi

# Собираем исполняемый Spring Boot JAR через Gradle daemon и останавливаем его в конце builder-слоя
RUN gradle :account:bootJar --daemon && \
    gradle --stop

# ─────────────────────────────────────────────────────────────
# ЭТАП 2: Запуск сервиса account в легковесном JRE-образе
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

# Создаем не-root пользователя для безопасного запуска приложения
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Копируем только готовый JAR из builder-стадии, чтобы в runtime-образ не попадали Gradle и исходники
COPY --from=builder /app/account/build/libs/*.jar app.jar

# Порт сервиса account
EXPOSE 8082

# Запускаем приложение от имени не-root пользователя
USER appuser

# JVM-оптимизации для контейнеров
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "app.jar"]
