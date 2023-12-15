package Server;

import Messages.JsonMessageExtractor;

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
            case "BROADCAST_REQ"-> commandBroadcastReq(message);
            case "PRIVATE_SEND"-> commandPrivateSend(message);
            case "GG_CREATE"-> commandGGCreate(message);
            case "GG_JOIN"-> commandGGJoin(message);
            case "GG_LEAVE"-> commandGGLeave(message);
            case "BYE"-> commandBye();
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

    }

    private void commandGGJoin(Map<String, String> message) {

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