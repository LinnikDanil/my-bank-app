# ─────────────────────────────────────────────────────────────
# ЭТАП 1: Сборка сервиса front внутри Docker
# ─────────────────────────────────────────────────────────────
FROM gradle:9.3.1-jdk25 AS builder
WORKDIR /app

# Копируем Gradle wrapper и build-файлы root/front/common, чтобы front мог собрать зависимость project(':common')
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN mkdir -p common account cash front notification transfer
COPY front/build.gradle front/build.gradle
COPY common/build.gradle common/build.gradle

# Копируем исходники common/front и OpenAPI-спецификации, нужные для генерации клиентов front
COPY common/src common/src
COPY front/src front/src
COPY openapi openapi

# Собираем исполняемый Spring Boot JAR через Gradle daemon и останавливаем его в конце builder-слоя
RUN gradle :front:bootJar --daemon && \
    gradle --stop

# ─────────────────────────────────────────────────────────────
# ЭТАП 2: Запуск сервиса front в легковесном JRE-образе
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

# Создаем не-root пользователя для безопасного запуска приложения
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Копируем только готовый JAR из builder-стадии, чтобы в runtime-образ не попадали Gradle и исходники
COPY --from=builder /app/front/build/libs/*.jar app.jar

# Порт сервиса front
EXPOSE 8086

# Запускаем приложение от имени не-root пользователя
USER appuser

# JVM-оптимизации для контейнеров
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0"

ENTRYPOINT ["java", "-jar", "app.jar"]
