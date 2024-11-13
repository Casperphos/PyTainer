FROM gradle:jdk23-alpine AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

COPY src ./src

RUN gradle build --no-daemon

FROM eclipse-temurin:23-jre-noble

# Installing Python and venv
RUN apt-get update && apt-get install -y python3 python3-venv && rm -rf /var/lib/apt/lists/*

# venv creation
RUN python3 -m venv /opt/venv

# Activating venv and installing pipreqs
RUN . /opt/venv/bin/activate && pip install pipreqs

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /app/script_upload /app/script_log

EXPOSE 2137

# Set the PATH to include venv's bin directory
ENV PATH="/opt/venv/bin:$PATH"

# Use a shell script as entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
