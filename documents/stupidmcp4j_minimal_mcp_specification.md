# stupidMCP4j Minimal MCP Specification

This document details the minimal JSON-RPC 2.0 messages required for **`stupidMCP4j`** to support both stateless/modern discovery and legacy handshake-based MCP clients over a single HTTP endpoint:

- **Transport:** HTTP `POST`
- **Path:** `/mcp`
- **Content-Type:** `application/json`

---

## 1. Summary of Minimal Methods

| # | Method | Direction | RPC Type | Purpose |
|---|---|---|---|---|
| **1** | `server/discover` | Client → Server | Request ↔ Response | **Mandatory:** Discovers supported versions, identity, and capabilities. |
| **2** | `initialize` | Client → Server | Request ↔ Response | Legacy connection handshake. |
| **3** | `notifications/initialized` | Client → Server | Notification | Legacy client confirmation (no RPC reply). |
| **4** | `tools/list` | Client → Server | Request ↔ Response | Returns tool metadata and JSON Schema definitions. |
| **5** | `tools/call` | Client → Server | Request ↔ Response | Executes a tool implementation via plugin. |
| **6** | `ping` | Client → Server | Request ↔ Response | Liveness and health check probe. |

---

## 2. Standard JSON-RPC 2.0 Wrapper Rules

1. **Request envelope**:
   - `jsonrpc`: Must strictly equal `"2.0"`.
   - `id`: String, integer, or null identifier (echoed back in responses). Notifications omit this field.
   - `method`: Name of the method to invoke.
   - `params`: Object containing method-specific parameters (optional if no arguments required).
2. **Success response envelope**:
   - `jsonrpc`: `"2.0"`
   - `id`: Matches request `id`.
   - `result`: Object containing method-specific return payload.
3. **Error response envelope**:
   - `jsonrpc`: `"2.0"`
   - `id`: Matches request `id` (or `null` if parse error).
   - `error`: Object with `code` (integer), `message` (string), and optional `data` (any).

---

## 3. Message Wire Formats & Specifications

### 3.1. `server/discover` (Modern Discovery)

Used by clients operating in stateless or per-request capability discovery mode to inspect protocol compatibility, identity, and features without maintaining session state.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "discover-1",
  "method": "server/discover",
  "params": {
    "_meta": {
      "io.modelcontextprotocol/protocolVersion": "2026-07-28",
      "io.modelcontextprotocol/clientInfo": {
        "name": "mcp-client",
        "version": "1.0.0"
      },
      "io.modelcontextprotocol/clientCapabilities": {}
    }
  }
}
```

* **Properties**:
  * `params._meta` *(object, optional)*: Contextual metadata for stateless negotiation.
  * `protocolVersion` *(string)*: Protocol revision supported or preferred by the caller.
  * `clientInfo` *(object)*: Identifier containing `name` and `version` of the calling client.
  * `clientCapabilities` *(object)*: Feature flags supported by the client (e.g., experimental features, roots, sampling).

#### Response Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "discover-1",
  "result": {
    "resultType": "complete",
    "supportedVersions": [
      "2026-07-28",
      "2024-11-05"
    ],
    "capabilities": {
      "tools": {
        "listChanged": false
      }
    },
    "_meta": {
      "io.modelcontextprotocol/serverInfo": {
        "name": "stupidMCP4j",
        "version": "1.0.0"
      }
    },
    "instructions": "stupidMCP4j lightweight tool server."
  }
}
```

* **Properties**:
  * `result.supportedVersions` *(list of string)*: Versions supported by server.
  * `result.capabilities` *(object)*: Advertised capabilities. `tools` indicates the server exposes tool invocation.
  * `result.capabilities.tools.listChanged` *(boolean)*: Set to `false` for minimal implementations since dynamic tool change notifications are not broadcast.
  * `result.serverInfo` *(object)*: Identifies the server implementation name and version.
  * `result.instructions` *(string, optional)*: Guidance text for the client/LLM explaining how to use the server.

---

### 3.2. `initialize` (Legacy Handshake)

Used by stateful or legacy MCP clients prior to running queries.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "init-1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "roots": {
        "listChanged": false
      },
      "sampling": {}
    },
    "clientInfo": {
      "name": "legacy-mcp-client",
      "version": "1.2.0"
    }
  }
}
```

* **Properties**:
  * `params.protocolVersion` *(string)*: Highest protocol version supported by the client.
  * `params.capabilities` *(object)*: Client-side capability definitions (e.g. `roots`, `sampling`).
  * `params.clientInfo` *(object)*: Client application name and version.

#### Response Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "init-1",
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {
        "listChanged": false
      }
    },
    "serverInfo": {
      "name": "stupidMCP4j",
      "version": "0.1.0"
    }
  }
}
```

* **Properties**:
  * `result.protocolVersion` *(string)*: The protocol version the server accepts.
  * `result.capabilities` *(object)*: Set of server features. Minimal implementations provide `tools`.
  * `result.serverInfo` *(object)*: Name and version of the server.

---

### 3.3. `notifications/initialized` (Client Handshake Acknowledgment)

Sent by clients after receiving the `initialize` response to signal readiness. Because this is a **Notification**, it contains no `id` property, and the server **must not** return an RPC response.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "method": "notifications/initialized",
  "params": {}
}
```

* **Properties**:
  * `id`: **Omitted**.
  * `method` *(string)*: Must be `"notifications/initialized"`.
  * `params` *(object, optional)*: Empty object or omitted.

#### Response Wire Format
* **HTTP Level**: HTTP `202 Accepted` or `204 No Content` with an empty body (or HTTP `200 OK` with an empty body). No JSON-RPC response envelope is sent.

---

### 3.4. `tools/list` (Tool Discovery)

Returns a list of all tools exposed by `stupidMCP4j`, complete with their JSON Schema parameter specifications.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "tools-1",
  "method": "tools/list",
  "params": {
    "cursor": "optional-pagination-token"
  }
}
```

* **Properties**:
  * `params.cursor` *(string, optional)*: Cursor token for pagination. Minimal servers that return all tools in one page can ignore this.

#### Response Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "tools-1",
  "result": {
    "tools": [
      {
        "name": "calculate_sum",
        "description": "Calculates the sum of two numbers.",
        "inputSchema": {
          "type": "object",
          "properties": {
            "a": {
              "type": "number",
              "description": "First number"
            },
            "b": {
              "type": "number",
              "description": "Second number"
            }
          },
          "required": ["a", "b"]
        }
      }
    ],
    "nextCursor": null
  }
}
```

* **Properties**:
  * `result.tools` *(array)*: List of tool descriptor objects.
    * `name` *(string)*: Unique identifier used in `tools/call`.
    * `description` *(string, optional)*: Description consumed by the client/LLM to decide invocation.
    * `inputSchema` *(object)*: Standard JSON Schema describing acceptable keys, types, and required parameters in `arguments`.
  * `result.nextCursor` *(string or null)*: Pagination token if more tools exist; `null` if all tools have been listed.

---

### 3.5. `tools/call` (Tool Invocation)

Invokes a specific registered tool and returns the outcome as content items.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "call-1",
  "method": "tools/call",
  "params": {
    "name": "calculate_sum",
    "arguments": {
      "a": 15,
      "b": 27
    }
  }
}
```

* **Properties**:
  * `params.name` *(string)*: Name matching a registered tool definition.
  * `params.arguments` *(object)*: Map of key-value pairs conforming to the tool's `inputSchema`.

#### Response Wire Format (Success)
```json
{
  "jsonrpc": "2.0",
  "id": "call-1",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "42"
      }
    ],
    "isError": false
  }
}
```

* **Properties**:
  * `result.content` *(array)*: Array of output content blocks.
    * `type` *(string)*: Discriminator type, typically `"text"` (or `"image"`, `"resource"`).
    * `text` *(string)*: Resulting output string (used when `type == "text"`).
  * `result.isError` *(boolean)*: Indicates if tool business logic encountered an expected failure (e.g., division by zero or invalid entity ID).

#### Response Wire Format (Tool Execution Failure / System Error)
When an unhandled exception or missing tool occurs, the server emits a standard JSON-RPC error:
```json
{
  "jsonrpc": "2.0",
  "id": "call-1",
  "error": {
    "code": -32601,
    "message": "Tool not found: calculate_sum",
    "data": {
      "requestedTool": "calculate_sum"
    }
  }
}
```

---

### 3.6. `ping` (Liveness / Health Check)

Simple round-trip check to confirm the server process is responsive.

#### Request Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "method": "ping",
  "params": {}
}
```

* **Properties**:
  * `params` *(object, optional)*: Empty object or omitted.

#### Response Wire Format
```json
{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "result": {}
}
```

* **Properties**:
  * `result` *(object)*: Empty JSON object (`{}`) indicating successful receipt.

---

## 4. Standard Error Codes Reference

When emitting JSON-RPC errors, `stupidMCP4j` should use standard error codes:

| Code | Constant | Meaning |
|---|---|---|
| `-32700` | `PARSE_ERROR` | Invalid JSON received. |
| `-32600` | `INVALID_REQUEST` | Payload does not conform to valid JSON-RPC 2.0 structure. |
| `-32601` | `METHOD_NOT_FOUND` | Method requested is not supported (i.e. not in the 6 minimal methods). |
| `-32602` | `INVALID_PARAMS` | Missing or invalid arguments (e.g. missing `name` on `tools/call`). |
| `-32603` | `INTERNAL_ERROR` | Internal server or unhandled exception. |
