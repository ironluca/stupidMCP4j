package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;
import static io.github.ironluca.stupidmcp4j.ConfigNV.mcpVersion;
import static io.github.ironluca.stupidmcp4j.ConfigNV.serverName;
import static io.github.ironluca.stupidmcp4j.ConfigNV.serverVersion;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.mapper;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;
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
 * Processes MCP version 2024-11-05 'initialize' message.
 * 
 * Request
 * <pre>
 * {@code {
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
        "protocolVersion": "2024-11-05",
        "capabilities": {},
        "clientInfo": {
        "name": "legacy-client",
        "version": "1.0.0"
        }
    }
    }}
 * </pre>
 * Response
 * <pre>
 * {@code {
    "jsonrpc": "2.0",
    "id": 1,
    "result": {
        "protocolVersion": "2024-11-05",
        "capabilities": {
        "tools": {
            "listChanged": false
        }
        },
        "serverInfo": {
        "name": "stupidMCP4j",
        "version": "1.0.0"
        }
    }
    }}
 * </pre>
 */
class InitializeProcessor {

    void process(JsonNode messageID, HttpExchange exchange, JsonNode bodyNode)throws IOException{

        JsonNode error=null;

        JsonNode clientProtocolVersionNode=bodyNode.at("/params/protocolVersion");
        if(clientProtocolVersionNode.isMissingNode()){

            error=makeError(messageID, -32602, "Missing protocol version");
            sendResponse(exchange, 400, error);
            return;

        }//if closing

        if(!mcpVersion.equals(clientProtocolVersionNode.asString())){

            error=makeError(messageID, -32022, "Unsupported protocol version");
            sendResponse(exchange, 400, error);
            return;

        }//if closing

        /*
        if(!mcpSupportedVersions.contains(clientProtocolVersionNode.asString())){

            error=makeError(messageID, -32022, "Unsupported protocol version");
            sendResponse(exchange, 400, error);
            return;

        }//if closing
        */

        ObjectNode response=mapper.createObjectNode();
        response.put("jsonrpc", jsonRPCSupportedVersion);
        response.set("id", messageID);

        ObjectNode result=mapper.createObjectNode();
        response.set("result", result);
        result.put("resultType", "complete");
        result.put("protocolVersion", mcpVersion);

        ObjectNode capabilities=mapper.createObjectNode();
        result.set("capabilities", capabilities);
        ObjectNode tools=mapper.createObjectNode();
        capabilities.set("tools", tools);
        tools.put("listChanged", false);

        ObjectNode serverInfo=mapper.createObjectNode();
        result.set("serverInfo", serverInfo);
        serverInfo.put("name", serverName);
        serverInfo.put("version", serverVersion);//Sed the exact version client wanted
        
        sendResponse(exchange, 200, response);

    }//process closing

}//class closing
