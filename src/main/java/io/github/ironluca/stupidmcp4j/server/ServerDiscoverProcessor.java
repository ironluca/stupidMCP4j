package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;
import static io.github.ironluca.stupidmcp4j.ConfigNV.mcpSupportedVersions;
import static io.github.ironluca.stupidmcp4j.ConfigNV.serverName;
import static io.github.ironluca.stupidmcp4j.ConfigNV.serverVersion;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.mapper;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/*
 * Copyright 2026 The stupidMCP4j Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This class processes server/discover method of MCP.
 * 
 * Request
 * <pre>
 * {@code {
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
    }}
 * </pre>
    Response
 * <pre>
    {@code {
    "jsonrpc": "2.0",
    "id": "discover-1",
    "result": {
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
    }}
 * </pre>
 * @see #process(JsonNode, HttpExchange, JsonNode)
 */
class ServerDiscoverProcessor {

    void process(JsonNode messageID, HttpExchange exchange, JsonNode bodyNode)throws IOException{

        JsonNode error=null;
        
        JsonNode clientProtocolVersionNode=bodyNode.at("/params/_meta/io.modelcontextprotocol~1protocolVersion");
        if(clientProtocolVersionNode.isMissingNode()){

            error=makeError(messageID, -32602, "Missing protocol version");
            sendResponse(exchange, 400, error);
            return;

        }//if closing

        if(!mcpSupportedVersions.contains(clientProtocolVersionNode.asString())){

            error=makeError(messageID, -32022, "Unsupported protocol version");
            sendResponse(exchange, 400, error);
            return;

        }//if closing

        ObjectNode response=mapper.createObjectNode();
        response.put("jsonrpc", jsonRPCSupportedVersion);
        response.set("id", messageID);

        ObjectNode result=mapper.createObjectNode();
        response.set("result", result);
        result.put("resultType", "complete");

        ArrayNode supportedVersions=mapper.createArrayNode();
        result.set("supportedVersions", supportedVersions);
        for(String v: mcpSupportedVersions)supportedVersions.add(v);

        ObjectNode capabilities=mapper.createObjectNode();
        result.set("capabilities", capabilities);
        ObjectNode tools=mapper.createObjectNode();
        capabilities.set("tools", tools);
        tools.put("listChanged", false);

        ObjectNode _meta=mapper.createObjectNode();
        result.set("_meta", _meta);
        ObjectNode serverInfo=mapper.createObjectNode();
        _meta.set("io.modelcontextprotocol/serverInfo", serverInfo);
        serverInfo.put("name", serverName);
        serverInfo.put("version", serverVersion);

        result.put("instructions", "Lightweight tool server.");

        sendResponse(exchange, 200, response);
    
    }//process closing

}//class closing
