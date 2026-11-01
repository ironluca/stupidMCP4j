package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.mcpVersion;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;

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
 * This class contains common method which support MCP protocol version 2026-07-28. This class is extended only by all the 
 * processor classes that process 2026-07-28 messages.
 * 
 * @author Ironluca
 */
class Abstract2026Processor {

    /**
     * This method checks the '_meta' property in 'params' property for version 2026-07-28 correctness.
     * The below properties need to be present:
     * <ul>
     *  <li>io.modelcontextprotocol/protocolVersion with value 2026-07-28</li>
     *  <li>io.modelcontextprotocol/clientCapabilities with an object, which can be empty</li>
     * </ul>
     * Example:
     * <pre>
     *"params": {
      "_meta": {
          "io.modelcontextprotocol/protocolVersion": "2026-07-28",
          "io.modelcontextprotocol/clientCapabilities":{}
        }
      }
    }
     * </pre>
     * In addition, it also checks for any header mismatch, if client sends such header. If the header is not sent, 
     * it is not checked, these checks are only performed for server/discover, tools/list and tools/call methods:
     * <ol>
     *  <li>MCP-Protocol-Version</li>
     *  <li>Mcp-Method</li>
     *  <li>Mcp-Name</li>
     * </ol>
     * @param messageID The id of the current message
     * @param bodyNode The complete body node of the message
     * @return The error structure, if there is an error or null, if there is none.
     */
    JsonNode evaluate_Meta_Header_Error(HttpExchange exchange, JsonNode messageID, JsonNode bodyNode){

        JsonNode error=null;

        //----------------- Mandatory _meta checks

        JsonNode clientProtocolVersionNode=bodyNode.at("/params/_meta/io.modelcontextprotocol~1protocolVersion");
        JsonNode clientCapabilityNode=bodyNode.at("/params/_meta/io.modelcontextprotocol~1clientCapabilities");

        //Check if protocol version is missing from _meta
        if(clientProtocolVersionNode.isMissingNode() || clientProtocolVersionNode.isNull()){

            error=makeError(messageID, -32602, "Missing protocol version. Include io.modelcontextprotocol/protocolVersion in _meta");
            return error;

        }//if closing

        //Check that the version is actually the one supported by the server
        if(!mcpVersion.equals(clientProtocolVersionNode.asString())){

            error=makeError(messageID, -32022, "Unsupported protocol version: "+clientProtocolVersionNode.asString());
            return error;

        }//if closing
        
        if(clientCapabilityNode.isMissingNode() || clientCapabilityNode.isNull()){

            error=makeError(messageID, -32602, "Missing client capabilities. Include io.modelcontextprotocol/clientCapabilities in _meta");
            return error;

        }//if closing


        //Optional header checks, only checked if sent by client, not otherwise
        String headerProtocolVersion=exchange.getRequestHeaders().getFirst("MCP-Protocol-Version");
        String headerMcpMethod=exchange.getRequestHeaders().getFirst("Mcp-Method");
        String headerMcpName=exchange.getRequestHeaders().getFirst("Mcp-Name");
        
        if(headerProtocolVersion!=null && !clientProtocolVersionNode.asString().equals(headerProtocolVersion)){

            error=makeError(messageID, -32020, "MCP-Protocol-Version does not match with what is provided in _meta");
            return error;

        }//if closing

        if(headerMcpMethod!=null){

            //NOTE: This will never be missing, otherwise it will not come this far
            String method=bodyNode.at("/method").asString();

            if(!method.equals(headerMcpMethod)){

                error=makeError(messageID, -32020, "Mcp-Method does not match with what is provided in _meta");
                return error;

            }//if closing

        }//if closing

        //This is intended, if a client send Mcp-Name for tools/list, then it is client libraries problem.
        //If a header is sent, it will be checked.
        if(headerMcpName!=null){

            JsonNode paramToolName=bodyNode.at("/params/name");
            if(paramToolName.isMissingNode() || 
               paramToolName.isNull() || 
               !paramToolName.asString().equals(headerMcpName)){

                error=makeError(messageID, -32020, "Mcp-Name does not match with what is provided in params");
                return error;

            }//if closing

        }//if closing

        return error;

    }//evaluate_Meta_Header_Error closing

}//class closing
