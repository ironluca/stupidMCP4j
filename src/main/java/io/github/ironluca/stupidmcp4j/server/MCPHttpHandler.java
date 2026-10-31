package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.server.ServerUtil.makeError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendHTTPError;
import static io.github.ironluca.stupidmcp4j.server.ServerUtil.sendResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import tools.jackson.databind.node.NullNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
class MCPHttpHandler implements HttpHandler {

    //This should be one per class
    private static final PrimaryDispatcher dispatcher=new PrimaryDispatcher();
    private static final Set<String> LOOPBACK_HOSTS = Set.of(
        "localhost",
        "127.0.0.1",
        "[::1]",
        "::1"
    );

    private String host;
    private int port;

    MCPHttpHandler(String host, int port){

        this.host=host;
        this.port=port;

    }//constructor closing

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {

            // 1. Inspect the HTTP method.
            String method = exchange.getRequestMethod();

            //We only support POST
            if (!"POST".equalsIgnoreCase(method)) {

                //Method Not Allowed
                exchange.getResponseHeaders().set("Allow", "POST");
                sendHTTPError(exchange, 405);
                return;

            }//if closing

            //Check Host header & Origin header.
            try{

                securityCheck(exchange);

            }catch(Exception e){

                sendHTTPError(exchange, 403);
                return;

            }//catch closing
            

            // 2. Inspect request Conent-type headers.
            String contentType =exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {

                //Unsupported Media Type
                sendHTTPError(exchange, 415);
                return;

            }//if closing

            // 3. Read the request body.
            String requestBody=null;
            try (InputStream input = exchange.getRequestBody()) {

                requestBody = new String(input.readAllBytes(),StandardCharsets.UTF_8);

            }catch(IOException ioe){

                //Make server error
                sendResponse(exchange, 500, makeError(NullNode.instance, -32603, "Internal server error"));
                return;

            }//catch closing

            // 4. Process the request.
            dispatcher.dispatch(exchange, requestBody);

        }catch(IOException ioe){throw ioe;}
         catch(Exception e){sendHTTPError(exchange, 500);}
         finally {

            // 5. Release the exchange resources.
            exchange.close();

        }//finally closing

    }//handle closing

    //============================= Internal Methods ==============================================//
    private void securityCheck(HttpExchange exchange) throws SecurityException {

        // 1. Always reject requests bearing an Origin header (blocks browser-originated cross-site requests)
        String sentOrigin = exchange.getRequestHeaders().getFirst("Origin");
        if (sentOrigin !=null)throw new SecurityException("Origin header rejected");

        // 2. If bound to a wildcard address, all host headers are intentionally permitted
        //User has explicitly configured this
        if ("0.0.0.0".equals(host) || "::".equals(host) || "[::]".equals(host)) return;

        //Check Host header
        String sentHostPort = exchange.getRequestHeaders().getFirst("Host");
        if (sentHostPort == null || sentHostPort.isBlank())throw new SecurityException("Missing Host header");
        
        sentHostPort = sentHostPort.trim().toLowerCase();


        String sentHost;

        if (sentHostPort.startsWith("[")) { // IPv6 e.g. [::1] or [::1]:8008

            int closingBracket = sentHostPort.indexOf(']');
            if (closingBracket == -1)throw new SecurityException("Malformed IPv6 Host header");

            sentHost = sentHostPort.substring(1, closingBracket);

        } else { // IPv4 or Hostname e.g. 127.0.0.1:8008 or localhost

            sentHost = sentHostPort.split(":")[0];

        }//else closing

        // 1. If server.json binds to a non-loopback IP/interface (e.g., 0.0.0.0 or LAN IP), skip loopback checks
        if(!LOOPBACK_HOSTS.contains(host))return;//The user has chosen a host which is not in the loopback address, e.g. 172.17.90.4, can't help

        // 2. Bound to loopback: sentHost MUST also be a valid loopback address
        if (!LOOPBACK_HOSTS.contains(sentHost))
            throw new SecurityException("Forbidden: Host header is not a valid loopback address (" + sentHost + ")");

    }//securityCheck closing

}//class closing

