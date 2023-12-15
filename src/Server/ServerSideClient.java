package Server;

import Messages.JsonMessage;
import Messages.JsonMessageExtractor;
import Messages.MessageError;
import Messages.MessageGoodStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ServerSideClient implements Runnable {
    public static final int NAME_MAX_LENGTH = 14;
    public static final int NAME_MIN_LENGTH = 3;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private boolean isLoggedIn;
    private Server relatedServer;
    private boolean hasJoinedGame = false;

    public ServerSideClient(PrintWriter writer, BufferedReader reader) {
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    public void run() {
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                ClientCommand command = SplitInput(inputLine);
                DetermineAction(command);
                System.out.println("Received from client: " + inputLine);
                writer.println("Server: Message received - " + inputLine);
            }
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
        this.username = message.get("username");
    }

    private void commandBroadcastReq(Map<String, String> message) {

    }
    private void commandPrivateSend(Map<String, String> message) {

    }

    private void commandGGCreate(Map<String, String> message) {
        String resp = "GG_CREATE_RESP";
        JsonMessage messageToSend;
        if (relatedServer.isGameCreated()) {
            messageToSend = new MessageError("8001");
        } else {
            messageToSend = new MessageGoodStatus();
            relatedServer.setGameCreated(true);
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
        if (!relatedServer.isGameCreated()) {
            messageToSend = new MessageError("8002");
        } else if(hasJoinedGame){
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

    public boolean isLoggedIn(){
        return isLoggedIn;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "ServerSideClient{" +
                "writer=" + writer +
                ", reader=" + reader +
                ", username='" + username + '\'' +
                '}';
    }
}