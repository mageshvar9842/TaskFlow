#!/bin/bash
# Task Flow - Run Script

# Check if JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
    echo "Warning: JAVA_HOME is not set. Using default java command."
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Run the application
echo "Starting Task Flow..."
mvn javafx:run
