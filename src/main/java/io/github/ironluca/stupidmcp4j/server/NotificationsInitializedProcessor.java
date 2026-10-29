package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.databind.JsonNode;

class NotificationsInitializedProcessor {

    void process(JsonNode messageID, HttpExchange exchange, JsonNode bodyNode)throws IOException{

        sendResponse(exchange, 204, null);

    }//process closing

}//class closing
