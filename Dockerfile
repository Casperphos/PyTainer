FROM gradle:jdk23-alpine AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

COPY src ./src

RUN gradle build --no-daemon

FROM eclipse-temurin:23-jre-noble

RUN apt-get update && apt-get install -y python3 python3-venv && rm -rf /var/lib/apt/lists/*

RUN python3 -m venv /opt/venv && . /opt/venv/bin/activate && pip install pipreqs

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/script_upload /app/script_log

EXPOSE 2137

ENV PATH="/opt/venv/bin:$PATH"

CMD ["/bin/bash", "-c", "source /opt/venv/bin/activate && exec java -jar app.jar"]
