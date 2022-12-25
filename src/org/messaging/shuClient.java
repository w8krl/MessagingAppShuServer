package org.messaging;

import java.io.*;
import java.net.*;
import java.util.*;        // required for Scanner

import org.json.simple.*;  // required for JSON encoding and decoding
import org.messaging.OpenRequest;

//import org.shuServer.*;

public class shuClient {


    private Socket socket;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private BufferedReader stdIn;

    public void displayMenu() {
        System.out.println();
    }


    public shuClient(Socket socket) {
        try {
            this.socket = socket;
            this.toServer = new PrintWriter(socket.getOutputStream(), true);
            this.fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.out.println("IO Error");
        }

    }

    public void ServerRequest() {
        try {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                // Parse user and build request
                Request req;
                Scanner sc = new Scanner(userInput);
                try {
                    switch (sc.next()) {
                        case "login" -> req = new LoginRequest(sc.skip(" ").nextLine());
                        case "open" -> req = new OpenRequest(sc.skip(" ").nextLine());
                        case "subscribe" -> req = new SubscribeRequest(sc.skip(" ").nextLine());
                        case "publish" -> req = new PublishRequest(sc.skip(" ").nextLine());
                        default -> {
                            System.out.println("ILLEGAL COMMAND");
                            continue;
                        }
                    }

                    // Send request to server
                    toServer.println(req);

                } catch (NoSuchElementException e) {
                    System.out.println("ILLEGAL COMMAND");
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("Error processing client request");
        }
    }

    public void listenToServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String serverResponse;

                while (socket.isConnected()) {
                    try {

                        // Read server response; terminate if null (i.e. server quit)

                        if ((serverResponse = fromServer.readLine()) == null)
                            break;

                        // Parse JSON response, then try to deserialize JSON
                        Object json = JSONValue.parse(serverResponse);
                        Response resp;

                        // Try to deserialize a success response
                        if (SuccessResponse.fromJSON(json) != null)
                            continue;

                        // Try to deserialize a list of messages
                        if ((resp = MessageListResponse.fromJSON(json)) != null) {
                            for (Message m : ((MessageListResponse) resp).getMessages())
                                System.out.println(m);
                            continue;
                        }

                        // Try to deserialize an error response
                        if ((resp = ErrorResponse.fromJSON(json)) != null) {
                            System.out.println(((ErrorResponse) resp).getError());
                            continue;
                        }

                        // Not any known response
                        System.out.println("PANIC: " + serverResponse +
                                " parsed as " + json);
                        break;
                    } catch (IOException e) {
                        System.out.println("Error reading server response");

                    }
                }
            }
        }).start();


    }

    public static void main(String[] args) throws IOException {

        final String HOSTNAME = "localhost";
        final int PORT = 8765;

        String activeChannel;

        try {
            Socket clientSocket = new Socket(HOSTNAME, PORT);
            shuClient client = new shuClient(clientSocket);
            client.listenToServer();
            client.ServerRequest();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + HOSTNAME);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    HOSTNAME);
        }

    }
}
