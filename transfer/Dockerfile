# ─────────────────────────────────────────────────────────────
# ЭТАП 1: Сборка сервиса transfer внутри Docker
# ─────────────────────────────────────────────────────────────
FROM gradle:9.3.1-jdk25 AS builder
WORKDIR /app

# Копируем Gradle wrapper и build-файлы всех модулей, чтобы Docker мог закешировать зависимости
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY common/build.gradle common/build.gradle
COPY account/build.gradle account/build.gradle
COPY cash/build.gradle cash/build.gradle
COPY front/build.gradle front/build.gradle
COPY notification/build.gradle notification/build.gradle
COPY transfer/build.gradle transfer/build.gradle

# Прогреваем зависимости без Gradle daemon, чтобы в builder-слое не оставался фоновый процесс
RUN gradle :transfer:dependencies --no-daemon

# Копируем исходники только тех модулей, которые реально нужны для сборки transfer
COPY common/src common/src
COPY transfer/src transfer/src
COPY openapi openapi

# Собираем исполняемый Spring Boot JAR автономно, без участия хост-машины
RUN gradle :transfer:bootJar --no-daemon

# ─────────────────────────────────────────────────────────────
# ЭТАП 2: Запуск сервиса transfer в легковесном JRE-образе
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

# Создаем не-root пользователя для безопасного запуска приложения
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Копируем только готовый JAR из builder-стадии, чтобы в runtime-образ не попадали Gradle и исходники
COPY --from=builder /app/transfer/build/libs/*.jar app.jar

# Порт сервиса transfer
EXPOSE 8084

# Запускаем приложение от имени не-root пользователя
USER appuser

# JVM-оптимизации для контейнеров
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "app.jar"]
