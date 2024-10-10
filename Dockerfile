FROM gradle:jdk23-alpine AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

COPY src ./src

RUN gradle build --no-daemon

FROM openjdk:23-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 2137

ENTRYPOINT ["java", "-jar", "app.jar"]