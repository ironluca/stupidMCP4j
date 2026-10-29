package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class ServerUtil {

    static final ObjectMapper mapper=new ObjectMapper();

    /**
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
        error.set("error", errorNode);
        error.put("code", errorCode);
        error.put("message", message);

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

            exchange.sendResponseHeaders(statusCode, 0);

        }//else closing

    }//sendResponse closing

    static final void sendHTTPError(HttpExchange exchange, int statusCode) throws IOException {

        exchange.sendResponseHeaders(statusCode, 0);

    }//sendResponse closing    

}//class closing
