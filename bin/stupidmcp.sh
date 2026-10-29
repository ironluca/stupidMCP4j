#!/usr/bin/env bash
set -e

# Fail fast if required variables are missing
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set. Please export JAVA_HOME=/path/to/jdk-17" >&2
    exit 1
fi

if [ -z "$MCP_HOME" ]; then
    echo "ERROR: MCP_HOME is not set. Please export MCP_HOME=/path/to/stupidMCP4j" >&2
    exit 1
fi

# Verify the java binary exists
JAVACMD="$JAVA_HOME/bin/java"
if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: Cannot find executable java at $JAVACMD" >&2
    exit 1
fi

# Build classpath using MCP_HOME
CP="$MCP_HOME/lib/*:$MCP_HOME/tools/*:$MCP_HOME/conf"

echo "Starting stupidMCP4j (Home: $MCP_HOME)..."
exec "$JAVACMD" \
    -cp "$CP" \
    io.github.ironluca.stupidmcp4j.server.MCPServer "$@"
