package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.mapper;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import io.github.ironluca.stupidmcp4j.tools.ToolRegistry;
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
 * {@code {
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
    }}
 * 
{@code {
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "tools": [
      {
        "name": "execute_query",
        "description": "Runs a read-only SQL query",
        "inputSchema": {
          "type": "object",
          "properties": {
            "sql": {
              "type": "string",
              "description": "SQL statement"
            }
          },
          "required": ["sql"]
        }
      }
    ]
  }
}}
 */
public class ToolsListProcessor {

    private ToolRegistry registry;

    ToolsListProcessor(){

        try{

            registry=ToolRegistry.getToolRegistry();

        }catch(Exception e){e.printStackTrace();}

    }//constructor closing

    void process(JsonNode messageID, HttpExchange exchange, JsonNode bodyNode)throws IOException{

        ObjectNode response=mapper.createObjectNode();
        response.put("jsonrpc", jsonRPCSupportedVersion);
        response.set("id", messageID);

        ObjectNode result=mapper.createObjectNode();
        response.set("result", result);
        result.put("resultType", "complete");
        result.set("tools", mapper.convertValue(registry.getToolList(), ArrayNode.class));

        sendResponse(exchange, 200, response);

    }//process closing

}//class closing
