package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.jsonRPCSupportedVersion;

import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.mapper;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

class PrimaryDispatcher {

    private ServerDiscoverProcessor serverDiscover=new ServerDiscoverProcessor();
    private InitializeProcessor initialize=new InitializeProcessor();
    private NotificationsInitializedProcessor notificationsInitialized=new NotificationsInitializedProcessor();
    private ToolsListProcessor toolsList=new ToolsListProcessor();
    private ToolsCallProcessor toolsCall=new ToolsCallProcessor();

    void dispatch(HttpExchange exchange, String requestBody) throws IOException{
        
        JsonNode error=null;

        JsonNode bodyNode=null;
        try{

            bodyNode=mapper.readTree(requestBody);

        }catch(JacksonException je){

            error=makeError(NullNode.instance, -32700, "Parse error");
            sendResponse(exchange, 400, error);
            return;

        }//catch closing

        String version=null;
        JsonNode id=null;
        String method=null;

        if(bodyNode.has("jsonrpc"))version=bodyNode.get("jsonrpc").asString(null);
        if(bodyNode.has("id"))id=bodyNode.get("id");
        if(bodyNode.has("method"))method=bodyNode.get("method").asString(null);

        if(version==null || version.isBlank()){
            
            error=makeError(id, -32600, "JSON-RPC Version not provided");
            sendResponse(exchange, 400, error);
            return;

        }//if closing

        if(method==null || method.isBlank()){
            
            error=makeError(id, -32600, "Method not provided");
            sendResponse(exchange, 400, error);
            return;
        
        }//if closing

        //Only MCP 2024-11-05 notifications/initialized method can be accepted without id
        if(id==null && !method.equals("notifications/initialized")){
            
            error=makeError(id, -32600, "Id not provided");
            sendResponse(exchange, 400, error);
            return;
        
        }//if closing        

        if(!version.equals(jsonRPCSupportedVersion)){
            
            error=makeError(id, -32022, "JSON-RPC version "+version+" is not supported");
            sendResponse(exchange, 400, error);
            return;
        
        }//if closing

        try{

            switch (method) {

                case "server/discover":{serverDiscover.process(id,exchange, bodyNode); break;}
                case "initialize":{initialize.process(id, exchange, bodyNode); break;}
                case "notifications/initialized":{notificationsInitialized.process(id, exchange, bodyNode); break;}
                case "tools/list":{toolsList.process(id, exchange, bodyNode); break;}
                case "tools/call":{toolsCall.process(id, exchange, bodyNode); break;}
                case "ping":break;

                default:{makeError(id, -32601, "Method not supported");sendResponse(exchange, 400, error); break;}
                    
            }//switch closing

        }catch(RuntimeException rte){throw rte;}
    
    }//dispatch closing

}//class closing
