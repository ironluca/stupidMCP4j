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

        sendResponse(exchange, 200, response);
    
    }//process closing

}//class closing
