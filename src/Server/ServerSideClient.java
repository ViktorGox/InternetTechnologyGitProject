package Server;

import Messages.JsonMessage;
import Messages.JsonMessageExtractor;
import Messages.MessageError;
import Messages.MessageGoodStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ServerSideClient implements Runnable {
    public static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{3,14}$";
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private boolean isLoggedIn;
    private boolean hasJoinedGame = false;
    private final Socket socket;

    public ServerSideClient(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                ClientCommand command = SplitInput(inputLine);
                DetermineAction(command);
                System.out.println("Received from client: " + inputLine);
            }
        } catch (IOException ignored) {
        } finally {
            closeSocket();
        }
    }

    // TODO: ask Gerralt: Does it matter if we close the socket or writer/reader?
    private void closeSocket() {
        try {
            Server.getInstance().removeClient(this);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientCommand SplitInput(String inputLine) {
        String[] split = inputLine.split(" ", 2);
        return new ClientCommand(split[0], split[1]);
    }

    public void DetermineAction(ClientCommand clientCommand) {
        Map<String, String> message = JsonMessageExtractor.extractInformation(clientCommand.getMessage());
        switch (clientCommand.getCommand()) {
            case "LOGIN" -> commandLogIn(message);
            case "BROADCAST_REQ" -> commandBroadcastReq(message);
            case "PRIVATE_SEND" -> commandPrivateSend(message);
            case "GG_CREATE" -> commandGGCreate(message);
            case "GG_JOIN" -> commandGGJoin(message);
            case "GG_LEAVE" -> commandGGLeave(message);
            case "BYE" -> commandBye();
            default -> commandError();
        }
    }

    private void commandLogIn(Map<String, String> message) {
        String username = message.get("username");
        String responseCommand = "LOGIN_RESP";

        JsonMessage finalMessage;
        if (isLoggedIn) {
            finalMessage = new MessageError("5000");
        } else if (!username.matches(VALID_USERNAME_REGEX)) {
            finalMessage = new MessageError("5001");
        } else if (Server.getInstance().containsUser(username)) {
            finalMessage = new MessageError("5002");
        } else {
            finalMessage = new MessageGoodStatus();
            isLoggedIn = true;
            this.username = username;
        }

        sendToClient(responseCommand, finalMessage);
    }

    private void commandBroadcastReq(Map<String, String> message) {

    }

    private void commandPrivateSend(Map<String, String> message) {

    }

    private void commandGGCreate(Map<String, String> message) {
        String resp = "GG_CREATE_RESP";
        JsonMessage messageToSend;
        if (Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8001");
        } else {
            messageToSend = new MessageGoodStatus();
            Server.getInstance().setGameCreated(true);
        }
        try {
            writer.println(resp + messageToSend.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void commandGGJoin(Map<String, String> message) {
        String resp = "GG_JOIN_RESP";
        JsonMessage messageToSend;
        if (!Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8002");
        } else if (hasJoinedGame) {
            messageToSend = new MessageError("8003");
        } else {
            messageToSend = new MessageGoodStatus();
        }
        try {
            writer.println(resp + messageToSend.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void commandGGLeave(Map<String, String> message) {

    }

    private void commandBye() {

    }

    private void commandError() {

    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getUsername() {
        return username;
    }

    private void sendToClient(String code, JsonMessage message) {
        try {
            writer.println(code + message.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ServerSideClient{" +
                "writer=" + writer +
                ", reader=" + reader +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerSideClient transferred)) return false;
        return username.equals(transferred.username);
    }
}