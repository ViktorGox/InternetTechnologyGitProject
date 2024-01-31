package Client;

import Shared.ClientCommand;
import Shared.Messages.JsonMessage;
import Shared.Messages.JsonMessageExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class Client implements OnClientExited {
    private UserInput userInput;
    private EncryptionHandler encryptionHandler;
    private boolean keepListening = true;
    private boolean guessingGame = false;
    public static final boolean DISPLAY_RAW_DEBUG = true;

    private Map<String, String> sessionKeys;

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
        if (DISPLAY_RAW_DEBUG) System.out.println(clientCommand);

        //TODO: convert to clientCommand.getMessage() to JSON.

        switch (clientCommand.getCommand()) {
            case "PING" -> handlePingPong();
            case "FILE_TRF" -> userInput.handleFireTransfer(clientCommand.getMessage());
            case "GG_CREATE_RESP" -> setGuessingGame(true);
            case "FILE_TRF_ANSWER" -> userInput.startFileTransferSend();
            case "LOGIN_RESP" -> handleEncryption(clientCommand.getMessage());
            case "REQ_PUBLIC_KEY" ->
                    handlePublicKeyRequest(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
        }
    }

    public void handlePublicKeyRequest(Map<String, String> message) {
        String sender = message.get("username");
        System.out.println(encryptionHandler.getPublicKey());
    }

    private void handleEncryption(String message) {
        if (!message.contains("OK")) return;
        encryptionHandler = new EncryptionHandler();
    }

    private void handlePingPong() {
        userInput.writer.println("PONG");
        if (DISPLAY_RAW_DEBUG) System.out.println("Heartbeat Test Successful");
    }

    private void startUserInput(PrintWriter writer, BufferedReader reader) {
        UserInput userInput = new UserInput(writer, reader, this);
        this.userInput = userInput;
        Thread thread = new Thread(userInput);
        thread.start();
    }

    public String getSessionKey(String username) {
        return sessionKeys.get(username);
    }
    @SuppressWarnings("rawtypes")
    public void send(Enum header, JsonMessage message) {
        try {
            userInput.writer.println(header + message.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("rawtypes")
    public void send(Enum header) {
        userInput.writer.println(header);
    }

    @Override
    public void onClientExited() {
        keepListening = false;
    }

    public boolean isGuessingGame() {
        return guessingGame;
    }

    public void setGuessingGame(boolean guessingGame) {
        this.guessingGame = guessingGame;
    }
}