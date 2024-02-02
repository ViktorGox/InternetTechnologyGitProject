package Client;

import Shared.ClientCommand;
import Shared.EncryptionUtils;
import Shared.Headers.EncryptedPrivateHeader;
import Shared.Messages.Encryption.MessageEncPrivateSend;
import Shared.Messages.Encryption.MessageReqPublicKeyResp;
import Shared.Messages.Encryption.MessageSessionKeyCreate;
import Shared.Messages.Encryption.MessageSessionKeyCreateResp;
import Shared.Messages.JsonMessage;
import Shared.Messages.JsonMessageExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
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
            case "REQ_PUBLIC_KEY_RESP" ->
                    handlePublicKeyResponse(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
            case "SESSION_KEY_CREATE" ->
                    handleSessionKeyCreate(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
            case "SESSION_KEY_CREATE_RESP" ->
                    handleSessionKeyCreateResp(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
            case "ENC_PRIVATE_RECEIVE" ->
                    handleEncPrivateReceive(JsonMessageExtractor.extractInformation(clientCommand.getMessage()));
        }
    }

    private void handleSessionKeyCreateResp(Map<String, String> extractInformation) {
        String awaitingMessage = encryptionHandler.findWaitingUser(extractInformation.get("username"));
        if (awaitingMessage == null) {
            //TODO: throw error that ok was received without being asked.
            return;
        }
        byte[] sessionKey = getSessionKey(extractInformation.get("username"));

        String encryptedMessage = encryptionHandler.encryptWithSessionKey(awaitingMessage, sessionKey);

        MessageEncPrivateSend messageBroadcast = new MessageEncPrivateSend(extractInformation.get("username"), encryptedMessage);
        send(EncryptedPrivateHeader.ENC_PRIVATE_SEND, messageBroadcast);

    }

    private void handleEncPrivateReceive(Map<String, String> extractInformation) {
        String decrMessage = encryptionHandler.decryptWithSessionKey(extractInformation.get("message"),
                getSessionKey(extractInformation.get("username")));
        System.out.println("Received decrpyted message: " + decrMessage);
    }

    /**
     * Received encrypted session key.
     */
    private void handleSessionKeyCreate(Map<String, String> extractInformation) {
        try {
            byte[] decryptedSessionKey = encryptionHandler.decryptWithPrivateKey(
                    EncryptionUtils.stringByteArrayToByteArray(extractInformation.get("session_key")),
                    encryptionHandler.getPrivateKey());
            System.out.println("Adding session key now. Related user: " + extractInformation.get("username"));
            encryptionHandler.addSessionKey(extractInformation.get("username"), decryptedSessionKey);

            MessageSessionKeyCreateResp messageJson = new MessageSessionKeyCreateResp("OK", extractInformation.get("username"));
            send(EncryptedPrivateHeader.SESSION_KEY_CREATE_RESP, messageJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Received public key from other user.
     */
    public void handlePublicKeyResponse(Map<String, String> message) {
        try {
            PublicKey otherPublicKey = EncryptionUtils.stringByteArrayToPublicKey(message.get("publicKey"));

            byte[] sessionKey = encryptionHandler.generateRandomKey();

            System.out.println("Adding session key now. Related user: " + message.get("username"));
            encryptionHandler.addSessionKey(message.get("username"), sessionKey);

            byte[] encryptedSessionKey = encryptionHandler.encryptWithPublicKey(sessionKey, otherPublicKey);

            MessageSessionKeyCreate jsonMessage = new MessageSessionKeyCreate(encryptedSessionKey, message.get("username"));
            send(EncryptedPrivateHeader.SESSION_KEY_CREATE, jsonMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return public key
     */
    public void handlePublicKeyRequest(Map<String, String> message) {
        String sender = message.get("username");

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
            userInput.writer.println(jsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getSessionKey(String username) {
        return encryptionHandler.getSessionKey(username);
    }

    public void addToWaitingList(String username, String message) {
        encryptionHandler.addWaitingUser(username, message);
    }

    @SuppressWarnings("rawtypes")
    public void send(Enum header) {
        userInput.writer.println(header);
    }

    @Override
    public void onClientExited() {
        keepListening = false;
    }

    public EncryptionHandler getEncryptionHandler() {
        return encryptionHandler;
    }
}