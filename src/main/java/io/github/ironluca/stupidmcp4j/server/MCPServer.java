package io.github.ironluca.stupidmcp4j.server;

import static io.github.ironluca.stupidmcp4j.ConfigNV.confDir;
import static io.github.ironluca.stupidmcp4j.ConfigNV.defaultHost;
import static io.github.ironluca.stupidmcp4j.ConfigNV.defaultPort;
import static io.github.ironluca.stupidmcp4j.ConfigNV.envStupidmcpHome;
import static io.github.ironluca.stupidmcp4j.ConfigNV.serverConf;
import static io.github.ironluca.stupidmcp4j.ConfigNV.toolsConf;

import com.sun.net.httpserver.HttpServer;

import io.github.ironluca.stupidmcp4j.tools.ToolRegistry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
public class MCPServer {

    private Path serverConfPath;
    private Path toolsConfPath;

    private String host=defaultHost;
    private int port=defaultPort;

    private ThreadPoolExecutor executor;

    private HttpServer server;

    private ToolRegistry toolRegistry;

    private void start(){

        //TODO - Uncomment after testing
        //String mcphome=System.getenv(envStupidmcpHome);

        System.out.println("##### stupidMCP4j #####");
        System.out.println("## Retrieving environment variables ...");
        //TODO - Remove this after testing
        String mcphome="/home/sanjay-d/workspaces/git-workspace/stupidMCP4j";
        if(mcphome==null || mcphome.isBlank()){

            System.out.println("Cannot proceed, STUPIDMCP_HOME environment variable has not been set");
            System.out.println("Exiting ...");
            System.exit(1);

        }//if closing
        System.out.println("Done ...");


        System.out.println("## Checking configuration files ...  ");
        //Get the relevant paths of the directory and files.
        serverConfPath=Paths.get(mcphome, confDir, serverConf);
        toolsConfPath=Paths.get(mcphome, confDir, toolsConf);

        //The tools.json must exist in conf directory, without this the server cannot operate
        if(!Files.exists(toolsConfPath)){

            System.out.println("Cannot proceed, the tools configuration does not exist, cannot proceed");
            System.out.println("Exiting ...");
            System.exit(1);

        }//if closing
        System.out.println("Done ...");


        System.out.println("## Iniializing connection aspects ...  ");
        //Initialize the host and port of the server, if a configuration is provided, otherwise default to the 
        //default values for these
        if(!Files.exists(serverConfPath)){

            System.out.println("No explicit server configuration was found, using defaults. Host: "+host+" and Port: "+port);
            serverConfPath=null;

        }//if closing
        else initHostPort();
        System.out.println("Done ...");


        System.out.println("## Iniializing tool registry ...  ");
        //Initialize the tools
        try{

            toolRegistry=ToolRegistry.getToolRegistry();

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Could not load tools ... below is the error");
            System.out.println(e.getMessage());
            System.out.println("Exiting ...");
            System.exit(1);
        
        }//catch closing
        System.out.println("Done ...");


        System.out.println("## Starting daemon ...  ");
        //Initialize and start the server
        //Assign an executor so requests run concurrently
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor = new ThreadPoolExecutor(
            corePoolSize,
            corePoolSize * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(256),
            new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure if overwhelmed
        );
        
        try{

            server = HttpServer.create();
            server.bind(new InetSocketAddress(host, port), 128);
            server.setExecutor(executor);
            server.createContext("/mcp", new MCPHttpHandler());
            server.start();

        }catch(BindException be){System.out.println("The port "+port+" is in use, cannot bind."); System.exit(1);}
         catch(IOException ioe){System.out.println("Encountered error during server initialization");System.exit(1);}

        System.out.println("## Server Started :: Listening on http://"+host+":"+port+"/mcp for incoming requests");

    }//start closing

    private void initHostPort(){

        if(serverConfPath==null)return;

        else{

            ObjectMapper mapper=new ObjectMapper();

            try{

                JsonNode config=mapper.readTree(serverConfPath.toFile());
                if(config.has("host"))host=config.get("host").asString();
                if(config.has("port"))port=config.get("port").asInt();

            }catch(JacksonException je){System.out.println("Could not read configuration in conf/"+serverConf+". Check for syntax errors.");}

        }//else closing

    }//initHostPort closing

    private void stop(){

        server.stop(2);

    }//close closing

    public static void main(String[] args) throws Exception {

        MCPServer mcp=new MCPServer();
        mcp.start();

        // Clean Ctrl+C interceptor - Stop the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            System.out.println("\nShutting down stupidMCP4j...");
            mcp.stop(); // Stops JDK HttpServer, drains thread pool, closes tools
            mcp.executor.shutdownNow();

            System.out.println("Server stopped cleanly.");

        }, "mcp-shutdown-hook"));      

    }//main closing

}//class closing
