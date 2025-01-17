package server;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import com.sun.net.httpserver.*;
import handlers.*;
import handlers.MainHandler;


/*
 * Starts server and listens to requests made, then sends them off to the appropriate handlers.
 */
public class Server {
    private static final int MAX_WAITING_CONNECTIONS = 12;
    private static final String DEFAULT_PORT = "8080";

    /**
     * Initializes and runs the server
     * @param portNumber Port number for accepting incoming client connections
     */
    private void run(String portNumber) {
        System.out.println("Initializing HTTP Server at port " + portNumber);

        HttpServer server;
        try {
            server = HttpServer.create(
                    new InetSocketAddress(Integer.parseInt(portNumber)),
                    MAX_WAITING_CONNECTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.setExecutor(null);

        System.out.println("Creating contexts");

        server.createContext("/user/register", new RegisterHandler());
        server.createContext("/user/login", new LoginHandler());
        server.createContext("/clear", new ClearHandler());
        server.createContext("/fill", new FillHandler());
        server.createContext("/load", new LoadHandler());
        server.createContext("/person", new PersonHandler());
        server.createContext("/event", new EventHandler());
        server.createContext("/", new MainHandler());

        System.out.println("Starting server");
        server.start();
        System.out.println("Server started");
    }

    /**
     * Main method for server program
     * @param args Port number for accepting incoming client connections
     */
    public static void main(String[] args) {
        String portNumber;

        if (args.length != 0) {
            portNumber = args[0];
        } else {
            portNumber = DEFAULT_PORT;
        }
        new Server().run(portNumber);
    }
}