#!/bin/bash
set -e

# Activate the virtual environment
source /opt/venv/bin/activate

# Run the Java application
exec java -jar app.jar