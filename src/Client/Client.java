package Client;

import Messages.JsonMessageExtractor;
import Messages.MessageFileTrfAnswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client implements OnClientExited {
    private UserInput userInput;
    private boolean keepListening = true;

    public static void main(String[] args) {
        new Client().start();
    }

    public Client() {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 1337);

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            startUserInput(writer, reader);
            userInput.addListener(this);
        } catch (IOException e) {
            System.err.println("Failed to connect to server.");
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            while (keepListening) {
                String received = userInput.reader.readLine();
                if (received == null) return;
                if (received.equals("PING")) handlePingPong();
                else if (received.contains("FILE_TRF") && !received.contains("ANSWER") && !received.contains("code"))
                    userInput.handleFireTransfer(received);
                else System.out.println("From Server: " + JsonMessageExtractor.extractInformationFromServer(received));
            }
        } catch (IOException e) {
            System.err.println("Lost connection with server.");
            throw new RuntimeException(e);
        } finally {
            userInput.closeStreams();
        }
    }
    private void handlePingPong() {
        userInput.writer.println("PONG");
        System.out.println("Heartbeat Test Successful");
    }

    private void startUserInput(PrintWriter writer, BufferedReader reader) {
        UserInput userInput = new UserInput(writer, reader);
        this.userInput = userInput;
        Thread thread = new Thread(userInput);
        thread.start();
    }

    @Override
    public void onClientExited() {
        keepListening = false;
    }
}
