package io.github.ironluca.stupidmcp4j;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;

public class Server {

    private HttpServer server;

    /*
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

// Crucial: assign an executor so requests run concurrently
int poolSize = Runtime.getRuntime().availableProcessors() * 4;
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    poolSize,
    poolSize * 2,
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(256),
    new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure if overwhelmed
);

server.setExecutor(executor);
server.start();
     */

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("HttpServer is present and working!");
    }    

}//class closing
