package Client;

import Shared.ClientCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                if (received == null) continue;
                ClientCommand clientCommand = new ClientCommand(received);
                handleReceived(clientCommand);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            userInput.closeStreams();
        }
    }

    private void handleReceived(ClientCommand clientCommand) {
        System.out.println(clientCommand);

        switch (clientCommand.getCommand()) {
            case "PING" -> handlePingPong();
            case "FILE_TRF" -> userInput.handleFireTransfer(clientCommand.getMessage());
            case "GG_GUESS_RESP" -> handleGuessResponse(clientCommand.getMessage());
            case "GG_CREATE_RESP", "GG_JOIN_RESP" -> handleJoiningGame(clientCommand.getMessage());
            case "GG_GUESS_START" -> userInput.setGgStarted(true);
            case "GG_GUESS_END" -> userInput.setJoinedGame(false);
            case "FILE_TRF_ANSWER" -> userInput.startFileTransferSend();
        }
    }

    private void handleGuessResponse(String message) {
        if(message.contains("0")){
            userInput.setJoinedGame(false);
        }
        userInput.setResponse(true);
    }

    private void handlePingPong() {
        userInput.writer.println("PONG");
        System.out.println("Heartbeat Test Successful");
    }

    private void handleJoiningGame(String message){
        if(message.contains("OK")){
            userInput.setJoinedGame(true);
        }
        userInput.setResponse(true);
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