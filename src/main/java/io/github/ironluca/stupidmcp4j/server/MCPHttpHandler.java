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

    private static final int MAX_REQ_LIMIT=1024*1024;//1 MB

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

        if(host!=null)this.host=host.toLowerCase();
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
            

            // 2. Inspect request Conent-Type headers.
            String contentType =exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || contentType.isBlank()) {

                //Unsupported Media Type
                sendHTTPError(exchange, 415);
                return;

            }//if closing
            else{

                String mimeType = contentType.split(";")[0].trim().toLowerCase();
                if (!"application/json".equals(mimeType)) {

                    sendHTTPError(exchange, 415);
                    return;

                }//if closing

            }//else closing

            // 3. Read the request body. Limit it to 1MB
            String requestBody=null;
            try (InputStream input = exchange.getRequestBody()) {

                //requestBody = new String(input.readAllBytes(),StandardCharsets.UTF_8);

                byte[] bytes=input.readNBytes(MAX_REQ_LIMIT+1);

                if(bytes.length>MAX_REQ_LIMIT){

                    //Payload Too Large
                    exchange.getResponseHeaders().set("Connection", "close");
                    sendHTTPError(exchange, 413);
                    return;

                }//if closing

                requestBody=new String(bytes, StandardCharsets.UTF_8);

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
    /**
     * Enforces request origin validation and host header sanitization to protect against
     * Cross-Site Request Forgery (CSRF) and DNS Rebinding attacks.
     *
     * <h3>Threat Model: DNS Rebinding</h3>
     * When a local HTTP server listens on loopback interfaces (e.g., {@code 127.0.0.1}, {@code localhost},
     * or {@code [::1]}) without authentication, it is vulnerable to exploitation via an end-user's web browser:
     * <ol>
     *   <li>A user accesses a malicious third-party site (e.g., {@code evil.com}) within their browser.</li>
     *   <li>The attacker-controlled authoritative DNS server resolves {@code evil.com} with an extremely short
     *       TTL (Time-to-Live, e.g., 1–2 seconds) to serve malicious client-side JavaScript.</li>
     *   <li>Subsequent DNS lookups by the browser for the same origin are dynamically answered with the loopback
     *       address {@code 127.0.0.1}.</li>
     *   <li>The browser, assuming the destination still complies with the Same-Origin Policy (SOP), dispatches
     *       asynchronous HTTP requests (e.g., via {@code fetch()} or {@code XMLHttpRequest}) directly to
     *       {@code http://evil.com:8008/mcp}.</li>
     *   <li>The physical connection reaches the local server runtime. However, the browser sets the HTTP
     *       {@code Host} header to {@code evil.com:8008}.</li>
     *   <li>Without explicit {@code Host} header inspection, an unauthenticated local daemon would process the
     *       payload, allowing external web domains to arbitrarily execute local tools, exfiltrate data, or
     *       invoke host routines.</li>
     * </ol>
     *
     * <h3>Defense Mechanisms Applied</h3>
     * <ul>
     *   <li><b>Zero-Tolerance {@code Origin} Validation:</b> Standard non-browser clients, desktop AI clients, CLI tools, and reverse
     *       proxies (e.g., Claude Desktop, Cursor, NGINX, curl) do not provide an {@code Origin} header.
     *       Web browsers universally append an {@code Origin} header on cross-site/POST requests and prevent
     *       client-side scripts from omitting or modifying it. Any incoming request carrying an {@code Origin}
     *       header (including empty or blank values) is immediately rejected.</li>
     *   <li><b>Wildcard Bind Bypass:</b> If the server is explicitly configured to listen on all interfaces
     *       ({@code 0.0.0.0}, {@code ::}, or {@code [::]}), the user has intentionally opted into universal network
     *       accessibility. In this mode, host filtering is bypassed, but the {@code Origin} block remains active.</li>
     *   <li><b>Normalization of RFC 3986 IPv6 Literals:</b> Parses bracketed IPv6 host headers (e.g., {@code [::1]:8008})
     *       and standard IPv4/hostname formats (e.g., {@code 127.0.0.1:8008}, {@code localhost}), stripping the trailing
     *       port delimiter to extract a normalized target host.</li>
     *   <li><b>Loopback Identity Enforcement:</b> If the daemon is configured to bind to a loopback address,
     *       the parsed {@code Host} header value must strictly belong to the allowed set of canonical loopback names
     *       ({@code 127.0.0.1}, {@code localhost}, {@code ::1}, {@code [::1]}). Any foreign or rebinding host name
     *       triggers an immediate {@link SecurityException}.</li>
     * </ul>
     *
     * @param exchange the incoming {@link HttpExchange} encapsulating request headers and network streams
     * @throws SecurityException if an {@code Origin} header is detected, the {@code Host} header is missing or malformed,
     *                           or the incoming {@code Host} fails loopback identity validation
     */
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

