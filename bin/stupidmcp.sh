#!/usr/bin/env bash
set -e

# Fail fast if required variables are missing
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set. Please export JAVA_HOME=/path/to/jdk-17" >&2
    exit 1
fi

if [ -z "$STUPIDMCP_HOME" ]; then
    echo "ERROR: STUPIDMCP_HOME is not set. Please export STUPIDMCP_HOME=/path/to/stupidMCP4j" >&2
    exit 1
fi

# Verify the java binary exists
JAVACMD="$JAVA_HOME/bin/java"
if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: Cannot find executable java at $JAVACMD" >&2
    exit 1
fi

# Build classpath using STUPIDMCP_HOME
CP="$STUPIDMCP_HOME/lib/*:$STUPIDMCP_HOME/plugin"

echo "Starting stupidMCP4j (Home: $STUPIDMCP_HOME)..."
exec "$JAVACMD" \
    -cp "$CP" \
    io.github.ironluca.stupidmcp4j.StupidMCP "$@"