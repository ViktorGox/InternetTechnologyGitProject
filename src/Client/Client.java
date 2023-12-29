package Client;

import Messages.JsonMessageExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final PrintWriter writer;
    private final BufferedReader reader;
    private boolean keepListening = true;

    public static void main(String[] args) {
        new Client().start();
    }

    public Client() {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 1337);

            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            startUserInput(writer);

        } catch (IOException e) {
            System.err.println("Failed to connect to server.");
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            while (keepListening) {
                // Gets stuck here on exit. Waits for a response from the server but one will never come if the userInput is closed.
                String received = reader.readLine();
                if(received == null) return;
                if(received.equals("PING")){
                    // COMMENT THIS TO TEST FAILED TO RESPONSE TO PING
                    writer.println("PONG");
                    // ABOVE
                    System.out.println("Heartbeat Test Successful");
                } else {
                    System.out.println("From Server: " + JsonMessageExtractor.extractInformationFromServer(received));
                }
            }
        } catch (IOException e) {
            System.err.println("Lost connection with server.");
            throw new RuntimeException(e);
        } finally {
            closeStreams();
        }
    }

    private void closeStreams() {
        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startUserInput(PrintWriter writer) {
        Thread userInput = new Thread(new UserInput(writer, this));
        userInput.start();
    }

    public void closeInputFromServer() {
        keepListening = false;
    }
}
