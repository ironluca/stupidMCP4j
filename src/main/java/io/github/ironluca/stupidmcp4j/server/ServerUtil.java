package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
 * A utility class that contains common useful methods. Check the method definitions for more details.
 * 
 * @author Ironluca
 */
class ServerUtil {

    static final ObjectMapper mapper=new ObjectMapper();

    /**
     * Makes an MCP error message.
     * 
     * <pre>
     * {@code {
        jsonrpc: "2.0";
        id?: string | number;
        error: {
            code: number;
            message: string;
            data?: unknown;
        }
        }}</pre>
     * @param messageID
     * @param errorCode
     * @param message
     * @return
     */
    static final JsonNode makeError(JsonNode messageID, int errorCode, String message){

        ObjectNode error=mapper.createObjectNode();

        error.put("jsonrpc", jsonRPCSupportedVersion);
        error.set("id", messageID);

        ObjectNode errorNode=mapper.createObjectNode();
        errorNode.put("code", errorCode);
        errorNode.put("message", message);
        error.set("error", errorNode);

        return error;

    }//makeError closing

    static final void sendResponse(HttpExchange exchange,
                                   int statusCode,
                                   JsonNode responseBody) throws IOException {

        if(responseBody!=null){

            byte[] responseBytes = responseBody.toString().getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");   
            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream output = exchange.getResponseBody()) {output.write(responseBytes);}

        }//if closing
        else{

            exchange.sendResponseHeaders(statusCode, -1);

        }//else closing

    }//sendResponse closing

    static final void sendHTTPError(HttpExchange exchange, int statusCode) throws IOException {

        exchange.sendResponseHeaders(statusCode, -1);

    }//sendResponse closing    

}//class closing
