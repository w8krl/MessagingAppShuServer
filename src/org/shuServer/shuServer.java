package org.shuServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.*;

public class shuServer {

    static class clientHandler extends Thread {

        Socket client;
        private PrintWriter fromClient;
        private BufferedReader toClient;

        public clientHandler(Socket socket) throws IOException {

            try{
                socket = client;
                fromClient = new PrintWriter(client.getOutputStream(),true);
                toClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        public void run() {
            try{
                String jsonInput;
                while ((jsonInput = toClient.readLine()) !=null){
                    Object json = JSONValue.parse(jsonInput);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        int portNumber =  8888;

        try(ServerSocket server = new ServerSocket(portNumber)){
            System.out.println("Server running");
            while(true){
                Socket client = server.accept();
                new clientHandler(client).start();
            }

        } catch (IOException e) {
            System.out.println("Server error");
            System.out.println(e.getMessage());
        }


    }
}
