# stupidMCP4j

> **KISS (Keep It Simple, Stupid) Model Context Protocol Server for Java.**
>A lightweight MCP server for Java. Expose new or existing Java functionality to AI agents without introducing an AI application framework.

> No Spring. No Project Reactor. No dynamic bytecode magic. No frameworks to worry about.

> `stupidMCP4j` is a deliberately minimal Java MCP server designed to expose new or existing Java code and infrastructure to AI agent runtimes without introducing heavyweight frameworks or requiring application rewrites.

> Its philosophy is simple: deploy a JAR, configure your tools, and run. It prioritizes simplicity, low overhead, and ease of integration over implementing every capability of the MCP specification.

`stupidMCP4j` is an ultra-lightweight, configuration-driven Model Context Protocol (MCP) server for Java 17+. It provides a bare-metal HTTP tool-execution server using standard Java primitives (`com.sun.net.httpserver`), standard JSON-RPC 2.0, and a single, predictable command interface.

---

## Why stupidMCP4j?

Some of the official Java MCP libraries often pull in massive reactive dependency graphs (Spring AI, Project Reactor, Jackson BOMs, annotation scanners). 

If you just need an enterprise AI agent (Claude, Cursor, custom agents) to securely trigger queries or run actions against your backend systems without bringing in 200MB of dependencies, you shouldn't need a framework.

* **Zero Framework Baggage:** Runs on standard JDK 17 LTS.
* **HTTP Only:** No messy STDIO stream leaks; runs as a clean, headless microservice or Docker sidecar.
* **Predictable Command Pattern:** One single interface: `Object execute(Map<String, Object> args) throws Exception`.
* **Config-Driven:** Configure tools, actions, and JSON schemas in a clean JSON configuration file.
* **Instant Cold Starts:** Boots in under 100ms with negligible memory overhead (<30MB heap).

> **MCP Compatibility:** stupidMCP4j implements the mandatory (MUST) requirements of MCP 2026-07-28 for its supported functionality: `server/discover`, `tools/list`, and `tools/call`.
> Optional capabilities and transport requirements outside this scope are not implemented. Full MCP 2026-07-28 feature coverage (with optional features) is not claimed.


## When to Use stupidMCP4j

`stupidMCP4j` is purpose-built as minimal MCP `2026-07-28` server to bridge existing Java infrastructure with modern AI agent interfaces.

---

### Core Use Cases

#### 1. Exposing Internal Java Logic to AI Without Rewrites
* **Scenario:** You maintain battle-tested Java calculation engines, legacy database adapters, or proprietary parsers. You want AI agents to invoke these routines without rewriting them in TypeScript or Python.
* **Why stupidMCP4j:** Instead of adding heavy reactive dependencies or refactoring an existing project to fit modern framework BOMs, compile your business logic into a standard JAR and drop it into `tools/`. Wire up the inputs in `conf/tools.json`, and your logic is instantly reachable via standard MCP.

#### 2. Resource-Constrained Environments & Cloud Sidecars
* **Scenario:** You are deploying tools as containerized sidecars in Kubernetes or serverless containers where memory overhead directly drives cloud cost.
* **Why stupidMCP4j:** Standard enterprise frameworks can easily consume 250MB–512MB of heap and require several seconds to warm up. By relying strictly on JDK 17+ core libraries (`com.sun.net.httpserver`), `stupidMCP4j` boots fast with a baseline heap footprint below 30MB.

#### 3. Adding AI Capabilities Without Changing Existing Applications
* **Scenario:** You have stable, production Java applications that cannot be easily modified, rebuilt, or redeployed just to accommodate emerging AI integration requirements. You want to expose selected capabilities to AI agents without introducing another dependency into those applications.
* **Why stupidMCP4j:** Run it as an independent service in `stupidMCP4j` alongside your existing infrastructure. It can connect to existing services, databases, or Java components and expose selected operations through MCP. Your production applications remain untouched, independently deployable, and free from MCP-specific dependencies.

#### 4. Minimal MCP Infrastructure for On-Premises and Air-Gapped Environments
* **Scenario:** You need to expose internal enterprise capabilities to AI agents in restricted, on-premises, or air-gapped environments. Deploying a full AI application framework merely to host a collection of MCP tools introduces unnecessary dependencies, runtime overhead, and operational complexity.
* **Why stupidMCP4j:** It provides a small, self-contained Java MCP server without requiring Spring, LangChain, reactive runtimes, or cloud services. Its minimal deployment footprint makes it practical to introduce MCP capabilities into tightly controlled enterprise environments without introducing an entire AI application stack.

#### 5. Controlled Enterprise Gateways Behind Reverse Proxies
* **Scenario:** Security teams require auditing, rate limiting, and credential checks before client applications (such as developer IDEs) access backend logic.
* **Why stupidMCP4j:** Operating as a local HTTP daemon allows you to place battle-tested proxies like NGINX or Apache Tomcat in front of the server to enforce mTLS, API keys, Bearer tokens, and request auditing before any call reaches tool execution.

---

### What stupidMCP4j Is Not

`stupidMCP4j` is deliberately limited in scope. It is an MCP server, not an AI application framework or agent runtime. Do not expect it to:
- **Build or orchestrate AI agents:** No agent loops, planning, reasoning, prompt management, or conversation orchestration.
- **Manage LLMs:** No model hosting, model integration, or model lifecycle management.
- **Provide AI memory or RAG:** No embeddings, vector databases, document retrieval, or conversational memory.
- **Replace application frameworks:** No dependency injection, application lifecycle framework, or Spring ecosystem integration.
- **Implement the entire MCP specification:** It implements the **mandatory requirements** for its supported MCP functionality, but does not attempt to implement every optional capability or transport.
- **Support Streamable HTTP or SSE:** Communication uses its deliberately simple HTTP POST transport. SSE, server-initiated streaming, and streaming notifications are not supported and **are not planned**.
- **Provide security infrastructure:** Authentication, authorization, TLS termination, rate limiting, and auditing are responsibilities of the surrounding deployment infrastructure.
> **Its responsibility is simple:** expose configured Java tools through a lightweight, self-contained MCP server.

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
   │  (JDK HttpServer + Thread Pool)        │
   └───────────────────┬────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
  [ tools/list ]              [ tools/call ]
  Returns static              Dispatches to:
  schemas from config         ToolHandler.execute(action, args)
```

1. **`server/discover`**: Responds with response as per 2026-07-28 standard.
2. **`initialize`**: Responds with standard MCP server capabilities as per 2024-11-05 standard.
3. **`notifications/initialized`**: Responds as per 2024-11-05 standard.
4. **`tools/list`**: Returns the list of available actions and schemas defined in your configuration.
5. **`tools/call`**: Dispatches the action to your singleton handler and returns standard MCP `CallToolResult` JSON.
6. **`ping`**: Responds as per 2026-07-28 standard.

---

## Quickstart & Installation

Get `stupidMCP4j` running in seconds without building from source or pulling framework dependencies.

### Prerequisites

* **Java 17+** installed (standard JDK. **Just a JRE will not work**)

---

### Step 1: Download & Unpack

Download the release archive (`stupidMCP4j-<version>-bin.zip`) and unpack it to your preferred installation directory:

```bash
unzip stupidMCP4j-1.0.0.zip
cd stupidMCP4j
```

The unpacked layout contains everything required to run:

```
stupidMCP4j/
├── bin/
│   ├── stupidmcp.sh       # Linux / macOS startup script
│   └── stupidmcp.cmd      # Windows startup script
├── conf/                  # Server configuration and tool schemas
├── aux/                   # Contains a properties file `aux.properties` which tools can access at runtime
└── tools/                 # Tool JARs or plugin classes
```
---

### Step 2: Set Environment Variables
Configure JAVA_HOME pointing to your JDK 17+ installation and MCP_HOME pointing to the unpacked stupidMCP4j folder:

Linux / macOS
```bash
export JAVA_HOME=/path/to/jdk-17
export MCP_HOME=/path/to/stupidMCP4j
export PATH=$MCP_HOME/bin:$PATH
```

Windows (Command Prompt)
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
set MCP_HOME=C:\stupidMCP4j
```

Windows (PowerShell)
```ps1
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:MCP_HOME = "C:\stupidMCP4j"
```
---

### Step 3: Start the Server
Run the startup script from the bin directory by changing to the bin directory:

Linux / macOS
```bash
chmod +x bin/stupidmcp.sh
./stupidmcp.sh
```

Windows
```cmd
stupidmcp.cmd
```

### Step 4: Stopping the Server
To stop the server `Ctl+c` is sufficient.

---

### Step 4: Verify with Ping
Send a probe to verify that the server is operational with curl or any other REST client:
```bash
curl -X POST http://127.0.0.1:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "ping-1",
    "method": "ping"
  }'
```
Expected Response:
```json
{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "result": {
    "resultType": "complete"
  }
}
```
---
## Tool Definition & Implementation

Adding capabilities to `stupidMCP4j` does not require implementing vendor interfaces or bundling framework dependencies into your project. Tools follow a simple convention: a standard public Java class and a declarative entry in `conf/tools.json`.

---

### 1. Implementing the Tool Handler

Any tool implementation must satisfy two requirements:

1. **Default Constructor:** The class must be `public` and contain an accessible, no-argument default constructor.
2. **Execute Method:** The class must implement an `execute` method matching this exact signature:
   ```java
   public Object execute(Map<String, Object> params) throws Exception
   ```

#### Tool Contract Details

* **Input (`Map<String, Object> params`):** Normalized map of arguments provided by the client. Omitted or `null` arguments from the client are passed as an empty map.
* **Return Value (`Object`):**
  * **`String`:** Placed directly into the response `content` block as text.
  * **`Map` / POJO:** Serialized to a JSON text string.
  * **`List`:** Serialized as JSON array in the `content`.
* **Error Handling:** If execution fails, throw an `Exception`. The server catches the exception, sets `"isError": true`, and forwards `exception.getMessage()` back to the MCP client.

#### Example: `PassthroughTool.java`

```java
package io.github.ironluca.stupidmcp4j.tools;

import java.util.Map;

/**
 * A simple passthrough tool demonstrating the stupidMCP4j tool convention.
 */
public class PassthroughTool {

    public PassthroughTool() {
        // Required no-args constructor
    }

    /**
     * Executes the tool invocation.
     *
     * @param params Key-value pairs matching the schema declared in tools.json.
     * @return String, Map, POJO, or List of items returned to the caller.
     * @throws Exception Thrown on failure; getMessage() provides the error payload.
     */
    public Object execute(Map<String, Object> params) throws Exception {
        String message = "This is a message from pass through tool";

        if (params.containsKey("message") && params.get("message") != null) {
            message = params.get("message").toString();
        }

        return message;
    }
}
```

---

### 2. Declaring Tools in `conf/tools.json`

Register your tools and publish their JSON Schema definitions in `conf/tools.json`. The server uses this configuration to answer `tools/list` requests and route incoming `tools/call` invocations.

```json
[
  {
    "name": "passthrough_tool",
    "title": "Pass Through Tool",
    "description": "A passthrough tool for testing, supports only one optional parameter called 'message' of string type",
    "implementation": "io.github.ironluca.stupidmcp4j.tools.PassthroughTool",
    "roles": [],
    "inputSchema": {
      "type": "object",
      "properties": {
        "message": {
          "type": "string",
          "description": "An optional message parameter."
        }
      }
    }
  }
]
```

#### Field Reference

| Field | Type | Description |
|---|---|---|
| `name` | String | Unique identifier (`^[a-zA-Z0-9._-]{1,128}$`). Used by clients in `tools/call`. |
| `title` | String | Human-readable display label. |
| `description` | String | Explains the tool's purpose and usage instructions to LLMs. |
| `implementation`| String | Fully qualified class name to load and instantiate dynamically. |
| `roles` | Array | Role-based access control list (leave empty `[]` for unrestricted access). **Not supported yet, giving or ommiting has no consequence.** |
| `inputSchema` | Object | Draft-7 compliant JSON Schema validating caller arguments. |

---

### 3. Packaging & Deployment

To deploy custom tools to `stupidMCP4j`:

1. Compile and package your tool classes into a standard JAR (e.g., using `mvn package` or `gradle jar`).
2. Copy the resulting JAR file and any required third-party dependencies into the `tools/` directory[cite: 2]:
   ```text
   stupidMCP4j/
   ├── conf/
   │   └── tools.json
   └── tools/
       ├── my-custom-tool.jar
       └── third-party-dependency.jar
   ```
3. Restart the server using `bin/stupidmcp.sh` (or `bin\stupidmcp.cmd` on Windows)[cite: 2]. All JARs placed in `tools/` are dynamically included in the server classpath at startup[cite: 2].

---

## Bootstrapping the Server

Configuring and starting `stupidMCP4j` is straightforward and managed through a single configuration file.

---

### Configuration: `conf/server.json`

Server network binding is defined in `conf/server.json`[cite: 4]:

```json
{
  "host": "127.0.0.1",
  "port": 8008
}
```

* **`host`**: The IP interface to bind (defaults to `127.0.0.1`)[cite: 4].
* **`port`**: The port to listen on for incoming HTTP JSON-RPC connections (e.g., `8008`)[cite: 4].

---

### Security & Reverse Proxy Recommendation

`stupidMCP4j` intentionally remains minimal and lightweight: it does not include native SSL/TLS termination or HTTP authentication layers. 

> **Production Recommendation:**  
> Keep the server bound to `127.0.0.1` / `localhost` and place a battle-tested reverse proxy—such as **NGINX**, **Apache Tomcat**, or **Caddy**—in front of it.

The reverse proxy handles:
* **HTTPS / TLS Termination** to secure transport.
* **Authentication & Authorization** (API Keys, Bearer Tokens, Basic Auth, mTLS).
* **Rate Limiting & IP Filtering** to guard tool endpoints from abuse.

#### Example: NGINX Reverse Proxy Configuration

Below is a minimal NGINX configuration snippet to enforce HTTPS, check a static Bearer API token, and reverse proxy requests to `stupidMCP4j`:

```nginx
server {
    listen 443 ssl;
    server_name mcp.yourdomain.com;

    ssl_certificate     /etc/ssl/certs/mcp.crt;
    ssl_certificate_key /etc/ssl/private/mcp.key;

    location /mcp {
        # Simple Bearer Token authentication check
        if ($http_authorization != "Bearer YOUR_SECRET_API_KEY") {
            return 401 '{"jsonrpc":"2.0","error":{"code":-32000,"message":"Unauthorized"}}';
        }

        proxy_pass [http://127.0.0.1:8008](http://127.0.0.1:8008);
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
---

## Auxiliary Tool Configuration (`aux.properties`)

Tools often need access to environmental settings, database connection URLs, API keys, or custom configuration flags without hardcoding them into Java source files.

`stupidMCP4j` provides a global configuration properties file located at `aux/aux.properties` (or included on the server's root classpath). Any custom tool can access these properties at runtime via the standard Java `ClassLoader`.

---

### Reading Configuration Inside a Tool

Inside your tool's `execute` method (or a helper method), load the configuration stream directly using `getResourceAsStream`:

```java
package io.github.ironluca.stupidmcp4j.tools;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfiguredTool {

    public ConfiguredTool() {}

    public Object execute(Map<String, Object> params) throws Exception {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("aux.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new IllegalStateException("aux.properties not found on classpath");
            }
        }

        String apiEndpoint = props.getProperty("external.service.url", "[https://api.default.local](https://api.default.local)");
        String timeoutMs = props.getProperty("external.service.timeout", "5000");

        // Tool logic using loaded properties...
        return "Connected to " + apiEndpoint + " with timeout " + timeoutMs + "ms";
    }
}
```

---

### File Format Example (`aux.properties`)

You can define any arbitrary key-value pairs required by your deployed tools:

```properties
# Database or Service Connections
external.service.url=[https://internal-api.enterprise.local/v1](https://internal-api.enterprise.local/v1)
external.service.timeout=3000

# Feature Flags
feature.enable_debug_logging=true
max.batch.size=250
```

---

### Best Practices

* **Cache When Appropriate:** Since `aux.properties` remains static during runtime, load and parse it once into a static or instance field rather than reading the stream on every tool call.
* **Fallback Defaults:** Use `props.getProperty(key, defaultValue)` to supply resilient defaults when specific keys are not declared.
* **Separation of Concerns:** Keep server binding settings in `conf/server.json` and tool schemas in `conf/tools.json`[cite: 3, 4]; reserve `aux.properties` purely for business logic configuration needed by your tools.
