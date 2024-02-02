package Client;

import Shared.ClientCommand;
import Shared.EncryptionUtils;
import Shared.Headers.EncryptedPrivateHeader;
import Shared.Messages.Encryption.MessageReqPublicKeyResp;
import Shared.Messages.JsonMessage;
import Shared.Messages.JsonMessageExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class Client implements OnClientExited {
    private UserInput userInput;
    private EncryptionHandler encryptionHandler;
    private boolean keepListening = true;
    public static final boolean DISPLAY_RAW_DEBUG = true;

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
            case "GG_GUESS_RESP" -> handleGuessResponse(clientCommand.getMessage());
            case "GG_CREATE_RESP", "GG_JOIN_RESP" -> handleJoiningGame(clientCommand.getMessage());
            case "GG_GUESS_START" -> handleStartGame(clientCommand.getMessage());
            case "GG_GUESS_END" -> userInput.setJoinedGame(false);
            case "FILE_TRF_ANSWER" -> userInput.startFileTransferSend();
            case "LOGIN_RESP" -> handleEncryption(clientCommand.getMessage());
            case "REQ_PUBLIC_KEY" ->
                    handlePublicKeyRequest(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
            case "REQ_PUBLIC_KEY_RESP" -> handlePublicKeyResponse(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
        }
    }

    /**
     * Received public key from other user.
     */
    public void handlePublicKeyResponse(Map<String, String> message) {
        System.out.println(EncryptionUtils.stringByteArrayToPublicKey(message.get("publicKey")));
    }

    /**
     * Return public key
     */
    public void handlePublicKeyRequest(Map<String, String> message) {
        String sender = message.get("username");
        if (DISPLAY_RAW_DEBUG) System.out.println("Client public key: " + encryptionHandler.getPublicKey());

        MessageReqPublicKeyResp sendMessage = new MessageReqPublicKeyResp(encryptionHandler.getPublicKey().getEncoded(), sender);
        send(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP, sendMessage);
    }

    private void handleStartGame(String message) {
        if (!message.contains("OK")) {
            userInput.setJoinedGame(false);
        }
        userInput.setGgStarted(true);
    }

    //change me
    private void handleEncryption(String message) {
        if (!message.contains("OK")) return;
        encryptionHandler = new EncryptionHandler();
    }

    private void handleGuessResponse(String message) {
        if (message.contains("0")) {
            userInput.setJoinedGame(false);
        }
        userInput.setResponse(true);
    }

    private void handlePingPong() {
        userInput.writer.println("PONG");
        if (DISPLAY_RAW_DEBUG) System.out.println("Heartbeat Test Successful");
    }

    private void handleJoiningGame(String message) {
        if (message.contains("OK")) {
            userInput.setJoinedGame(true);
        }
        userInput.setResponse(true);
    }

    private void startUserInput(PrintWriter writer, BufferedReader reader) {
        UserInput userInput = new UserInput(writer, reader, this);
        this.userInput = userInput;
        Thread thread = new Thread(userInput);
        thread.start();
    }

    @SuppressWarnings("rawtypes")
    public void send(Enum header, JsonMessage message) {
        try {
            String jsonMessage = header + message.mapToJson();
            System.out.println("In client send: " + jsonMessage);
            userInput.writer.println(jsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSessionKey(String username) {
        return encryptionHandler.getSessionKey(username);
    }

    @SuppressWarnings("rawtypes")
    public void send(Enum header) {
        userInput.writer.println(header);
    }

    @Override
    public void onClientExited() {
        keepListening = false;
    }
}