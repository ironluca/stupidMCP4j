package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.mcpSupportedVersions;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;

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
     * @param messageID The id of the current message
     * @param bodyNode The complete body node of the message
     * @return The error structure, if there is an error or null, if there is none.
     */
    JsonNode evaluate_metaError(JsonNode messageID, JsonNode bodyNode){

        JsonNode error=null;

        JsonNode clientProtocolVersionNode=bodyNode.at("/params/_meta/io.modelcontextprotocol~1protocolVersion");
        JsonNode clientCapabilityNode=bodyNode.at("/params/_meta/io.modelcontextprotocol~1clientCapabilities");

        if(clientProtocolVersionNode.isMissingNode()){

            error=makeError(messageID, -32602, "Missing protocol version. Include io.modelcontextprotocol/protocolVersion in _meta");
            return error;

        }//if closing

        if(!mcpSupportedVersions.contains(clientProtocolVersionNode.asString())){

            error=makeError(messageID, -32022, "Unsupported protocol version: "+clientProtocolVersionNode.asString());
            return error;

        }//if closing
        
        if(clientCapabilityNode.isMissingNode()){

            error=makeError(messageID, -32602, "Missing client calabilities. Include io.modelcontextprotocol/clientCapabilities in _meta");
            return error;

        }//if closing

        return error;

    }//evaluate_metaError closing

}//class closing
