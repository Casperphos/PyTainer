FROM gradle:jdk23-alpine AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

COPY src ./src

RUN gradle build --no-daemon

FROM openjdk:23-slim

RUN apt-get update && apt-get install -y python3 python3-pip
RUN pip install pipreqs

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/script_upload /app/script_log

EXPOSE 2137

ENTRYPOINT ["java", "-jar", "app.jar"]