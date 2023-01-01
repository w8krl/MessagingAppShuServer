package org.messaging;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.*;

public class shuServer {

    static class Clock {
        private long t;

        public Clock() {
            t = 0;
        }

        public synchronized long tick() {
            return ++t;
        }
    }

    static class clientHandler extends Thread {

        // Store user socket mapping
        private static HashMap<String, Socket> userSockets = new HashMap<>();

        // map of user / channel subscriptions
        private static HashMap<String, HashSet<String>> subscriptions = new HashMap<>();

        // map of active users per channel - for broadcasting
        private static HashMap<String, HashSet<String>> activeChannelUsers = new HashMap<>();

        private static ArrayList<Message> messages = new ArrayList<>();


        // Active channel - null or one.
        private String activeChannel;

        private static Clock clock = new Clock();

        private Socket client;
        private PrintWriter toClient;
        private BufferedReader fromClient;
        private String user;

        private int read;

        public clientHandler(Socket socket) throws IOException {

            try {
                client = socket;
                toClient = new PrintWriter(client.getOutputStream(), true);
                fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                user = null;
                read = 0;
            } catch (IOException e) {
                System.out.println("Error reading client input");
            }
        }

        public void displaySubscriptions() {
            // for testing only
            int subCount = subscriptions.size();
            System.out.printf("Displaying Active channel subscriptions (%d users)\n", subCount);
            if (subCount > 0) {
                for (String channel : subscriptions.keySet()) {
                    HashSet<String> users = subscriptions.get(channel);
                    System.out.println("Channel: " + channel + ", User name: " + users.toString());
                }
                System.out.printf("\n");
            }
        }

        public void displayConnections() {

            int subCount = userSockets.size();

            System.out.printf("Displaying active connections (%d):\n", subCount);
            if (subCount > 0) {

                Iterator<Map.Entry<String, Socket>> iter = userSockets.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Socket> entry = iter.next();
                    String key = entry.getKey();
                    Socket value = entry.getValue();
                    System.out.println("User: " + key + ", Socket: " + value);
                }
                System.out.printf("\n");
            }
        }

        synchronized void messageBroadCast(String activeChannel, Message message) {
            Socket clientSocket;
            try {
                // Only one message will ever be transmitted per broadcast,
                // using list for compatibility with MessageListResponse
                List<Message> broadCastMessage = new ArrayList<>();
                broadCastMessage.add(message);

                // Get list of subs - only those connected via open session
                HashSet<String> broadcastList = activeChannelUsers.get(activeChannel);

                // Iterate list of subs and print via message list resp (takes a list and does the JSON bit)
                for (String user : broadcastList) {
                    if (!user.equals(this.user)) {
                        clientSocket = userSockets.get(user);
                        PrintWriter clientTo = new PrintWriter(clientSocket.getOutputStream(), true);
                        clientTo.println(new MessageListResponse(broadCastMessage)); // send to actively connected users
                    }
                }
            } catch (Exception e) {
                System.out.println("Error during broadcast");
            }
        }

        public void run() {
            try {
                String input;
                while ((input = fromClient.readLine()) != null) {

                    long ts = clock.tick();

                    Object json = JSONValue.parse(input);
                    Request request;

                    // Auth required to perform other actions - omitting password
                    if (user == null && (request = LoginRequest.fromJSON(json)) != null) {
                        // set login name
                        // store user, store user socket
                        user = ((LoginRequest) request).getName();
                        synchronized (clientHandler.class) {
                            userSockets.put(user, client);
                        }
                        System.out.printf("%s logged in\n", user);
                        // response acknowledging the login request
                        toClient.println(new SuccessResponse());
                        continue;
                    }

                    // OPEN CHANNEL - ADD USER TO ACTIVE CHANNEL
                    if (user != null && (request = OpenRequest.fromJSON(json)) != null) {
                        activeChannel = ((OpenRequest) request).getChannel();

                        synchronized (clientHandler.class) {
                            HashSet<String> users;
                            if (!activeChannelUsers.containsKey(activeChannel)) {
                                // if channel doesn't exist, automatically create it
                                users = new HashSet<>();
                            } else {
                                // If channel exists add client
                                users = activeChannelUsers.get(activeChannel);
                            }
                            users.add(user);
                            activeChannelUsers.put(activeChannel, users);
                        }
                        toClient.println(new SuccessResponse());

                        System.out.printf("%s connected to channel %s\n", user, activeChannel);
                        continue;
                    }

                    // DISCONNECT CHANNEL ACTIVE USER - ELSE WILL CONTINUE TO RECEIVE REALTIME UPDATES
                    if (user != null && (request = CloseRequest.fromJSON(json)) != null) {
                        String channelName = ((CloseRequest) request).getChannel();

                        // update active channel connections
                        synchronized (clientHandler.class) {
                            HashSet<String> users;
                            if (activeChannelUsers.containsKey(channelName)) {
                                users = activeChannelUsers.get(channelName);
                                users.remove(user); //remove
                                activeChannelUsers.put(channelName, users); //update list without user
                                System.out.printf("%s disconnected from channel: %s\n", user, activeChannel);
                                activeChannel = null; // finished publishing to channel (for now)
                                toClient.println(new SuccessResponse());
                            } else {
                                toClient.println(new ErrorResponse("CHANNEL" + channelName + " NOT FOUND: " + user));
                            }
                        }
                        continue;
                    }


                    if (user != null && (request = SubscribeRequest.fromJSON(json)) != null) {

                        synchronized (clientHandler.class) {
                            String channelName = ((SubscribeRequest) request).getChannel();
                            HashSet<String> users;
                            if (!subscriptions.containsKey(channelName)) {
                                // if channel doesn't exist, automatically create it
                                users = new HashSet<>();
                            } else {
                                // If channel exists add client
                                users = subscriptions.get(channelName);
                            }
                            users.add(user);
                            subscriptions.put(channelName, users);
                            toClient.println(new SuccessResponse());

                            System.out.printf("%s subscribed to channel %s\n", user, channelName);
//                            displaySubscriptions(); // print to server terminal for debug
                            continue;
                        }
                    }

                    // Unsubscribe request
                    if (user != null && (request = UnubscribeRequest.fromJSON(json)) != null) {

                        synchronized (clientHandler.class) {
                            String channelName = ((UnubscribeRequest) request).getChannel();
                            HashSet<String> users;

                            try {
                                if (subscriptions.containsKey(channelName)) {
                                    users = subscriptions.get(channelName);
                                    users.remove(user);
                                    subscriptions.put(channelName, users);
                                    toClient.println(new SuccessResponse());
                                    System.out.printf("%s unsubscribed from channel %s\n",
                                            user, channelName);
                                } else {
                                    toClient.println(new ErrorResponse("NO SUCH CHANNEL: " + channelName));
                                }

                            } catch (Exception e) {
                                System.out.printf("Error unsubscribing from channel\n" +
                                        "%s", e.getMessage());
                            }
//                            displaySubscriptions(); // print to server terminal for debug
                            continue;
                        }
                    }


                    if (user != null && (request = PublishRequest.fromJSON(json)) != null) {

                        if (activeChannel == null) {
                            toClient.println(new ErrorResponse("No channel selected to publish to.\nPlease open a channel."));
                            continue;
                        } else {
                            Message message = new Message(((PublishRequest) request).getMessage(), user, activeChannel, ts);
                            synchronized (clientHandler.class) {
                                messages.add(message);
                            }
                            messageBroadCast(activeChannel, message);
                            toClient.println(new SuccessResponse());
                            System.out.printf("%s published: \"%s\" to channel: %s\n",
                                    user, message.getBody(), activeChannel);
                        }
                        continue;

                    }

                    // Specific get request - i.e. user requested a fromDate (GetRequest)
                    if (user != null && (request = GetRequest.fromJSON(json)) != null) {

                        List<Message> filteredMessages;
                        long dateFrom = ((GetRequest) request).getTimestamp();
                        // synchronized access to the shared message board
                        synchronized (clientHandler.class) {
                            filteredMessages = messages.stream()
                                    .filter(msg -> msg.getChannel().contains(activeChannel))
                                    .filter(msg -> msg.getTimestamp() >= dateFrom)
                                    .collect(Collectors.toList());
                        }
                        // response: list of filtered messages
                        toClient.println(new MessageListResponse(filteredMessages));
                        System.out.printf("GET request from %s in channel %s [time >= %d]\n",
                                user, activeChannel, ((GetRequest) request).getTimestamp());

                        continue;
                    }

                    // quit request? Must be logged in; no response
                    if (user != null && QuitRequest.fromJSON(json) != null) {
                        fromClient.close();
                        toClient.close();
                        return;
                    }

                    // Finally
                    if (user == null) toClient.println(new ErrorResponse("Auth required"));
                    else toClient.println(new ErrorResponse("ILLEGAL REQUEST"));
                }
            } catch (IOException e) {
                System.out.println("Exception while connected");
            }
        }
    }

    public static void main(String[] args) {

        int portNumber = 8765;

        System.out.println("Server initialising");

        try (ServerSocket serverSocket = new ServerSocket(portNumber);) {
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("New connection from " + client.getLocalPort());
                new clientHandler(client).start();
            }
        } catch (IOException e) {
            System.out.printf("Server error on port: %d\n", portNumber);
            System.out.println(e.getMessage());
        }
    }

}
