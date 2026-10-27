# stupidMCP4j

> **KISS (Keep It Simple, Stupid) Model Context Protocol Server for Java.**  
> No Spring. No Project Reactor. No dynamic bytecode magic. Zero framework bloat.

`stupidMCP4j` is an ultra-lightweight, configuration-driven Model Context Protocol (MCP) server for Java 17+. It provides a bare-metal HTTP/S tool-execution server using standard Java primitives (`com.sun.net.httpserver`), standard JSON-RPC 2.0, and a single, predictable command interface.

---

## Why stupidMCP4j?

Official Java MCP libraries often pull in massive reactive dependency graphs (Spring AI, Project Reactor, Jackson BOMs, annotation scanners). 

If you just need an enterprise AI agent (Claude, Cursor, custom agents) to securely trigger queries or run actions against your backend systems without bringing in 200MB of dependencies, you shouldn't need a framework.

* **Zero Framework Baggage:** Runs on standard JDK 11 or 17 LTS.
* **HTTP/S Only:** No messy STDIO stream leaks; runs as a clean, headless microservice or Docker sidecar.
* **Predictable Command Pattern:** One single interface: `Object execute(String action, Map<String, Object> args)`.
* **Config-Driven:** Configure tools, actions, and JSON schemas in a clean JSON configuration file.
* **Instant Cold Starts:** Boots in under 100ms with negligible memory overhead (<30MB heap).

---

## How It Works

`stupidMCP4j` strips away the complexity of MCP into a classic Command Dispatcher model:

```
[ AI Client / Claude / Cursor ]
              │
              │  POST /mcp (JSON-RPC 2.0)
              ▼
   ┌────────────────────────────────────────┐
   │          stupidMCP4j Engine            │
   │  (JDK HttpServer + Thread Pool)       │
   └───────────────────┬────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
  [ tools/list ]              [ tools/call ]
  Returns static              Dispatches to:
  schemas from config         ToolHandler.execute(action, args)
```

1. **`initialize`**: Responds with standard MCP server capabilities.
2. **`tools/list`**: Returns the list of available actions and schemas defined in your configuration.
3. **`tools/call`**: Dispatches the action to your singleton handler and returns standard MCP `CallToolResult` JSON.

---
