FROM amazoncorretto:25-alpine-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
