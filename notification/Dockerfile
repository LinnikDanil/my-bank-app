FROM amazoncorretto:25-alpine-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
