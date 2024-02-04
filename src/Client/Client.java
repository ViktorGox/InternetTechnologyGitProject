package Client;

import Shared.ClientCommand;
import Shared.EncryptionUtils;
import Shared.Headers.EncryptedPrivateHeader;
import Shared.Headers.OtherHeader;
import Shared.MessageFactory;
import Shared.Messages.Bye.MessageLeft;
import Shared.Messages.Encryption.*;
import Shared.Messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

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
        System.out.println(clientCommand);
        JsonMessage createdMessage = MessageFactory.convertToMessageClass(clientCommand);
        // Parse or unknown command error handling.
        if(createdMessage instanceof MessageError) {
            if(((MessageError) createdMessage).getCode().equals("0")) {
                System.out.println("Failed to parse the given data to json.");
            }
            if(((MessageError) createdMessage).getCode().equals("1")) {
                System.out.println("Failed to find a command with the given header.");
            }
            return;
        }

        switch (clientCommand.getCommand()) {
            case "PING" -> handlePingPong();
            case "FILE_TRF" -> userInput.handleFireTransfer(createdMessage);
            case "GG_GUESS_RESP" -> handleGuessResponse(createdMessage);
            case "GG_CREATE_RESP", "GG_JOIN_RESP" -> handleJoiningGame(createdMessage);
            case "GG_GUESS_START" -> handleStartGame(createdMessage);
            case "GG_GUESS_END" -> userInput.setJoinedGame(false);
            case "FILE_TRF_ANSWER" -> handleFileTransferAnswer(createdMessage);
            case "LOGIN_RESP" -> handleEncryption(createdMessage);
            case "REQ_PUBLIC_KEY" -> handlePublicKeyRequest(createdMessage);
            case "REQ_PUBLIC_KEY_RESP" -> handlePublicKeyResponse(createdMessage);
            case "SESSION_KEY_CREATE" -> handleSessionKeyCreate(createdMessage);
            case "SESSION_KEY_CREATE_RESP" -> handleSessionKeyCreateResp(createdMessage);
            case "ENC_PRIVATE_RECEIVE" -> handleEncPrivateReceive(createdMessage);
            case "LEFT" -> handleLeft(createdMessage);
        }
    }

    private void handleLeft(JsonMessage jsonMessage) {
        MessageLeft message = (MessageLeft) jsonMessage;
        System.out.println(message.getUsername() + " left.");
    }

    private void handleSessionKeyCreateResp(JsonMessage jsonMessage) {
        MessageSessionKeyCreateResp message = (MessageSessionKeyCreateResp) jsonMessage;

        String awaitingMessage = encryptionHandler.findWaitingUser(message.getUsername());
        if (awaitingMessage == null) {
            return;
        }
        byte[] sessionKey = getSessionKey(message.getUsername());

        String encryptedMessage = encryptionHandler.encryptWithSessionKey(awaitingMessage, sessionKey);

        MessageEncPrivateSend messageBroadcast = new MessageEncPrivateSend(message.getUsername(), encryptedMessage);
        send(EncryptedPrivateHeader.ENC_PRIVATE_SEND, messageBroadcast);

    }

    private void handleEncPrivateReceive(JsonMessage jsonMessage) {
        MessageEncPrivateSend message = (MessageEncPrivateSend) jsonMessage;
        System.out.println("PRIVATE "+jsonMessage);
        String decrMessage = encryptionHandler.decryptWithSessionKey(message.getMessage(),
                getSessionKey(message.getUsername()));

        System.out.println("Decrypted received message: " + decrMessage);
    }

    /**
     * Received encrypted session key.
     */
    private void handleSessionKeyCreate(JsonMessage jsonMessage) {
        MessageSessionKeyCreate message = (MessageSessionKeyCreate) jsonMessage;

        try {
            byte[] decryptedSessionKey = encryptionHandler.decryptWithPrivateKey(
                    EncryptionUtils.stringByteArrayToByteArray(message.getSessionKey()),
                    encryptionHandler.getPrivateKey());
            encryptionHandler.addSessionKey(message.getUsername(), decryptedSessionKey);

            MessageSessionKeyCreateResp messageJson = new MessageSessionKeyCreateResp("OK", message.getUsername());
            send(EncryptedPrivateHeader.SESSION_KEY_CREATE_RESP, messageJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Received public key from other user.
     */
    public void handlePublicKeyResponse(JsonMessage jsonMessage) {
        if ((jsonMessage instanceof MessageError)) {
            return;
        }
        MessageReqPublicKeyResp message = (MessageReqPublicKeyResp) jsonMessage;

        try {
            PublicKey otherPublicKey = EncryptionUtils.stringByteArrayToPublicKey(message.getPublicKey());

            byte[] sessionKey = encryptionHandler.generateRandomKey();

            encryptionHandler.addSessionKey(message.getUsername(), sessionKey);

            byte[] encryptedSessionKey = encryptionHandler.encryptWithPublicKey(sessionKey, otherPublicKey);

            MessageSessionKeyCreate result = new MessageSessionKeyCreate(encryptedSessionKey, message.getUsername());
            send(EncryptedPrivateHeader.SESSION_KEY_CREATE, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return public key
     */
    public void handlePublicKeyRequest(JsonMessage jsonMessage) {

        MessageReqPublicKey message = (MessageReqPublicKey) jsonMessage;

        String sender = message.getUsername();

        MessageReqPublicKeyResp sendMessage = new MessageReqPublicKeyResp(encryptionHandler.getPublicKey().getEncoded(), sender);
        send(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP, sendMessage);
    }

    private void handleFileTransferAnswer(JsonMessage jsonMessage) {
        MessageFileTrfAnswer message = (MessageFileTrfAnswer) jsonMessage;

        if (message.getAnswer().equals("true")) {
            userInput.startFileTransferSend(message.getUuid());
        } else {
            System.out.println("File Transfer was rejected");
        }
    }

    private void handleStartGame(JsonMessage jsonMessage) {
        if ((jsonMessage instanceof MessageError)) {
            userInput.setJoinedGame(false);
        }
        userInput.setGgStarted(true);
    }

    private void handleEncryption(JsonMessage jsonMessage) {
        if ((jsonMessage instanceof MessageGoodStatus)) {
            encryptionHandler = new EncryptionHandler();
        }
    }

    private void handleGuessResponse(JsonMessage jsonMessage) {
        MessageGuess message = (MessageGuess) jsonMessage;

        if (message.getGuess().equals("0")) {
            userInput.setJoinedGame(false);
        }
        userInput.setResponse(true);
    }

    private void handlePingPong() {
        userInput.writer.println("PONG");
    }

    private void handleJoiningGame(JsonMessage jsonMessage) {
        if ((jsonMessage instanceof MessageGoodStatus)) {
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
        if (!isLoggedIn()) return null;
        return encryptionHandler.getSessionKey(username);
    }

    public void addToWaitingList(String username, String message) {
        if (!isLoggedIn()) {
            return;
        }
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

    public boolean isLoggedIn() {
        return encryptionHandler != null;
    }

    public EncryptionHandler getEncryptionHandler() {
        return encryptionHandler;
    }
}