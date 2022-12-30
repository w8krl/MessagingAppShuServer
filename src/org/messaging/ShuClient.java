package org.messaging;

import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ShuClient {

    final static String HOSTNAME = "localhost";
    final static int PORT = 8765;

    // Client socket
    private Socket socket;

    // I/O
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private BufferedReader stdIn;

    // User can sub to more than one channel
    private HashSet<String> myChannels = new HashSet<>();

    // Menu options
    private final Integer[] OPT_ARR = new Integer[]{1, 2, 3, 4,5, 9};
    private final List<Integer> OPT_LIST = new ArrayList<>(Arrays.asList(OPT_ARR));

    // Identity parameters
    private final int IDENTITY_LENGTH = 4;
    private String identity;

    // Active channels
    private String activeChannel;
    private boolean closed = false;
    private Scanner sc;


    public String getActiveChannel() {
        return activeChannel;
    }

    public void setActiveChannel(String activeChannel) {
        this.activeChannel = activeChannel;
    }


    public String getIdentity() {
        return identity;
    }

    public void displayChannels(){

        System.out.printf("Listing subscribed channels:");
        if(myChannels.size() > 0){
            for (String channel : myChannels) {
                System.out.println(channel);
            }
        } else {
            System.out.println("You aren't connected to any channels");
        }

    }

    public void setIdentity() {

        System.out.println("Welcome to the app, enter username to continue...");
        int failAtt = 0;
        String userInput;
        try {
            while ((userInput = stdIn.readLine()) != null && identity == null) {
                if (failAtt == 3) throw new AuthException();
                Scanner sc = new Scanner(userInput);
                String id = sc.next();
                if (id.length() < IDENTITY_LENGTH) {
                    System.out.printf("Min %d chars required, please try again\n",
                            IDENTITY_LENGTH);
                    failAtt++;
                } else {
                    this.identity = id;
                    toServer.println(new LoginRequest(identity));
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AuthException aex) {
            System.out.println(aex.getMessage());
            closeConnections();
        }
    }


    private void OpenChannel() {

        System.out.println("Create or connect to a channel to publish messages");
        System.out.println("You will be subscribed automatically.");
        System.out.println("Enter a channel name:");
        // Continue to select channel

        Request req;
        String userInput;
        try {
            userInput = stdIn.readLine();
            Scanner sc = new Scanner(userInput);
            String newChannel = sc.next();

            if (activeChannel != null && !activeChannel.equals(newChannel)) {
                System.out.printf("You will be disconnected from %s" +
                        ", continue? [y/n]", activeChannel);

                if (!stdIn.readLine().toLowerCase().equals("y")) {
                    System.out.println("Aborting operation");
                    return;
                } else {
                    // disconnect current active channel
                    toServer.println(new CloseRequest(activeChannel));
                }
            }

            setActiveChannel(newChannel);
            myChannels.add(newChannel);
            req = new OpenRequest(newChannel);
            toServer.println(req);

            // Sub - potentially move this to a server function for open handler
            req = new SubscribeRequest(newChannel);
            toServer.println(req);

            // View channel - realtime connection
            enterChannel();

        } catch (Exception e) {
            System.out.println("Error configuring channel");
        }
    }

    private void enterChannel() {
        System.out.println("You are connected to: " + activeChannel);
        System.out.print("Keywords: \"#quit\" to disconnect ");
        System.out.println("|| \"#fromDate\" (to apply timestamp filter)");


        Request req;
        try {
            String userInput;
            while (true) {
                userInput = stdIn.readLine();

                if (userInput.equals("#quit")) {
                    toServer.println(new CloseRequest(activeChannel));
                    setActiveChannel(null);
                    break; // still remain subscribed
                }


                if (userInput.equals("#fromDate")) {
                    long fromDate;
                    try {
                        System.out.println("Enter date to filter from: (number)");
                        Scanner in = new Scanner(System.in);
                        fromDate = in.nextLong();
                        toServer.println(new GetRequest(activeChannel, fromDate));

                    } catch (Exception e) {
                        System.out.println("invalid input");
                    }
                    continue;
                }


                if (userInput != null) {
                    toServer.println(new PublishRequest(userInput));
                }
            }
        } catch (IOException e) {
            System.out.println("I/O error occurred");
        }


    }

    public void exitChannel() {

        try {
            Request req = new UnubscribeRequest(activeChannel);
            toServer.println(req);
            activeChannel = null;
        } catch (Exception e) {
            System.out.println("Error disconnecting from channel");
        }

    }

    private void Subscribe() {
        // Continue to sub channel
        System.out.println("Enter channel name to receive instance messages:");
        Request req;
        String userInput;
        try {
            userInput = stdIn.readLine();
            Scanner sc = new Scanner(userInput);
            String subChannel = sc.next();
            req = new SubscribeRequest(subChannel);
            myChannels.add(subChannel);
            toServer.println(req);
        } catch (IOException e) {
            System.out.println("Error during channel subscription");
        }
    }



    private void Unsubscribe() {
        // Continue to sub channel

        displayChannels();

        // Still allows user to send an erroneous request, proof that it is handled at the server
        try{
            String userInput = stdIn.readLine();
            Request req;
            req = new UnubscribeRequest(userInput);
            toServer.println(req);
        } catch (IOException e) {
        }
    }
    private void GetMessages() {
        enterChannel();

    }

    public boolean closed() {
        return this.closed;
    }

    // console based UI
    public void displaySessionUI() {

        int selectedOption;
        if (identity == null) setIdentity();

        do {
            System.out.println("Logged in as: " + identity);
            System.out.println("#################################");
            System.out.println("# SHU Chatroom");
            System.out.println("#################################");
            System.out.println("Please select an option below:");
            System.out.println("1) Connect & publish to channel (open, publish)");
            System.out.println("2) Subscribe to channel");
            System.out.println("3) Unsubscribe from channel");
            System.out.println("4) View all messages for my channels");
            System.out.println("5) Show my channels");
            System.out.println("9) Quit App");

            try {
                selectedOption = Integer.parseInt(stdIn.readLine());

                if (selectedOption == 9)
                    break;

                switch (selectedOption) {
                    case 1 -> OpenChannel();
                    case 2 -> Subscribe();
                    case 3 -> Unsubscribe();
                    case 4 -> GetMessages();
                    case 5 -> displayChannels();
                    default -> throw new IllegalStateException("Unexpected value: " + selectedOption);
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (OPT_LIST.contains(selectedOption));

        closeConnections();


    }

    public void listenToServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String serverResponse;

                while (socket.isConnected() && !Thread.currentThread().isInterrupted()) {
                    try {

                        // Read server response; terminate if null (i.e. server quit)

                        if ((serverResponse = fromServer.readLine()) == null)
                            break;

                        // Parse JSON response, then try to deserialize JSON
                        Object json = JSONValue.parse(serverResponse);
                        Response resp;

                        // Try to deserialize a success response
                        if (SuccessResponse.fromJSON(json) != null) {
                            continue;
                        }

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
                        System.out.println("CRITICAL ERROR: " + serverResponse +
                                " parsed as " + json);
                        break;
                    } catch (IOException e) {
                        System.out.println("Error reading server response");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();

    }

    public void closeConnections() {
        try {
            toServer.close();
            fromServer.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    // Constructor
    public ShuClient() {

        try {
            Socket clientSocket = new Socket(HOSTNAME, PORT);
            this.socket = clientSocket;
            this.toServer = new PrintWriter(socket.getOutputStream(), true);
            this.fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.stdIn = new BufferedReader(new InputStreamReader(System.in));
            listenToServer();
            displaySessionUI();
        } catch (IOException e) {
            System.out.println("IO Error");
        }
    }

    public static void main(String[] args) throws IOException {
        new ShuClient();
    }

}
