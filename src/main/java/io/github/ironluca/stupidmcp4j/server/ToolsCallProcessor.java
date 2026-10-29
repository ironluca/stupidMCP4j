package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.mapper;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.sun.net.httpserver.HttpExchange;

import io.github.ironluca.stupidmcp4j.tools.ToolDefinition;
import io.github.ironluca.stupidmcp4j.tools.ToolRegistry;
import tools.jackson.core.type.TypeReference;
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
 * Request
 * <pre>
 * {@code {
    "jsonrpc": "2.0",
    "id": "call-1",
    "method": "tools/call",
    "params": {
        "name": "passthrough_tool",
        "arguments":{"message":"This should get returned back"}
    }
    }}
    </pre>
 * Response
    <pre>
    {@code {
    "jsonrpc": "2.0",
    "id": "call-1",
    "result": {
        "resultType": "complete",
        "content": [
        {
            "type": "text",
            "text": "This should get returned back"
        }
        ],
        "isError": false
    }
    }}
  </pre>
 * ToolsCallProcessor
 */
class ToolsCallProcessor {

    private ToolRegistry registry;

    private SchemaRegistry schemaFactory;

    ToolsCallProcessor(){

        try{

            registry=ToolRegistry.getToolRegistry();

            schemaFactory=SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

        }catch(Exception e){e.printStackTrace();}

    }//constructor closing

    void process(JsonNode messageID, HttpExchange exchange, JsonNode bodyNode)throws IOException{

        JsonNode error=null;

        try{

            JsonNode toolNameNode=bodyNode.at("/params/name");
            JsonNode argumentsNode=bodyNode.at("/params/arguments");

            //Check if the tool name property has been at all provided
            if(toolNameNode.isMissingNode()){

                error=makeError(messageID, -32602, "Tool name not provided");
                sendResponse(exchange, 400, error);
                return;

            }//if closing

            String toolName=null;
            if(toolNameNode.isString())toolName=toolNameNode.asString();

            //Check if the tool name is provided
            if(toolName==null || toolName.isBlank()){

                error=makeError(messageID, -32602, "Tool name not provided");
                sendResponse(exchange, 400, error);
                return;

            }//if closing

            //Check if the tool with the given name actually exist
            Method toolMethod=registry.getToolMethod(toolName);
            if(toolMethod==null){

                error=makeError(messageID, -32602, "Tool with name "+toolName+" does not exist");
                sendResponse(exchange, 400, error);
                return;

            }//if closing

            //Check if the tool object exists
            Object toolObject=registry.getToolObject(toolMethod);
            if(toolObject==null){

                error=makeError(messageID, -32603, "Internal server error");
                sendResponse(exchange, 500, error);
                return;

            }//if closing

            //Get the too definition, this contains the schema
            ToolDefinition toolDef=registry.getToolDefinition(toolName);
            ObjectNode schemaNode=toolDef.inputSchema;

            //Check if the parameter is actually provided, it is not mandatory by specification.
            //It also cannot be 'null' but if it is, we a forgiving and consider it not to be provided.
            if(!argumentsNode.isMissingNode() && !argumentsNode.isNull()){

                //Validate the parameter against schema. If validation fails send each error as a separate object in 'content' 
                //of the response. Set the 'isError' to true.
                Schema schema=schemaFactory.getSchema(schemaNode);
                List<com.networknt.schema.Error> errors=schema.validate(argumentsNode);

                if(!errors.isEmpty()){

                    ObjectNode response=makeEmptyResponse(messageID, true);
                    ArrayNode content=(ArrayNode)response.get("result").get("content");

                    for(int i=0;i<errors.size();i++)
                        content.add(makeContent(errors.get(i)));

                    sendResponse(exchange, 200, response);
                    return;

                }//if closing

            }//if closing
            

            //Prepare the params, it any argument is at all provided
            Map<String, Object> params = new HashMap<>();//Initialize to an empty map
            if(!argumentsNode.isMissingNode() && !argumentsNode.isNull() && argumentsNode.isObject()){

                params=mapper.convertValue(argumentsNode, new TypeReference<Map<String, Object>>() {});

            }//if closing


            //Invoke the tool and process the result
            try{

                ObjectNode response=makeEmptyResponse(messageID, false);
                ArrayNode content=(ArrayNode)response.get("result").get("content");

                Object result=toolMethod.invoke(toolObject, params);

                //Process the result, if it is a list, it gets serialized as a single text line
                if(result==null)content.add(makeContent("Tool did not produce any response"));
                else content.add(makeContent(result));

                sendResponse(exchange, 200, response);
                return;

            }catch(Exception e){

                //If there are any exceptions, send the message of the exception
                ObjectNode response=makeEmptyResponse(messageID, true);
                ArrayNode content=(ArrayNode)response.get("result").get("content");
                content.add(makeContent(e.getMessage()));

                sendResponse(exchange, 200, response);
                return;

            }//catch closing

        }catch(Exception e){

            e.printStackTrace();

            error=makeError(messageID, -32603, "Internal server error while executing tool.");
            sendResponse(exchange, 500, error);
            return;

        }//catch closing

    }//process closing

    private ObjectNode makeEmptyResponse(JsonNode messageID, boolean isError){

        ObjectNode response=mapper.createObjectNode();
        response.put("jsonrpc", jsonRPCSupportedVersion);
        response.set("id", messageID);

        ObjectNode result=mapper.createObjectNode();
        response.set("result", result);
        result.put("resultType", "complete");
        result.set("content", mapper.createArrayNode());
        result.put("isError", isError);

        return response;

    }//makeEmptyResponse closing

    private ObjectNode makeContent(Object data){

        ObjectNode contentNode=mapper.createObjectNode();
        contentNode.put("type", "text");
        if(data==null)contentNode.put("text", "No response is available");
        else if(data instanceof String)contentNode.put("text", (String)data);
        //First write produces JSON String, next one converts to escaped string
        else contentNode.put("text", mapper.writeValueAsString(data));

        return contentNode;

    }//makeContent closng

}//class closing
