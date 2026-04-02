FROM amazoncorretto:25-alpine-jdk AS build
WORKDIR /workspace
COPY build/libs/*.jar app.jar

FROM amazoncorretto:25-alpine-jdk
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
