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

}//class closing

