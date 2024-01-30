package Server;

import Messages.Broadcast.MessageBroadcast;
import Messages.*;
import Messages.PrivateMessage.PrivateReceiveMessage;
import Shared.ClientCommand;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ServerSideClient implements Runnable {
    public static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{3,14}$";
    private final boolean PERFORM_PING_PONG = false;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private PingPongInteraction pingPongInteraction;
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
                System.out.println("Received from client: " + inputLine);
                ClientCommand command = new ClientCommand(inputLine);
                DetermineAction(command);
            }
        } catch (IOException ignored) {
        } finally {
            closeSocket();
        }
    }

    protected void closeSocket() {
        try {
            Server.getInstance().removeClient(this);
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void DetermineAction(ClientCommand clientCommand) {
        Map<String, String> message = null;
        if (clientCommand.getMessage() != null) {
            message = JsonMessageExtractor.extractInformation(clientCommand.getMessage());
        }
        switch (clientCommand.getCommand()) {
            case "LOGIN" -> commandLogIn(message);
            case "BROADCAST_REQ" -> commandBroadcastReq(message);
            case "PRIVATE_SEND" -> commandPrivateSend(message);
            case "GG_CREATE" -> commandGGCreate(message);
            case "GG_JOIN" -> commandGGJoin(message);
            case "GG_GUESS" -> commandGG_Guess(message);
            case "GG_LEAVE" -> commandGGLeave(message);
            case "FILE_TRF" -> commandFileTransfer(message);
            case "FILE_TRF_ANSWER" -> commandFileTransferAnswer(message);
            case "BYE" -> commandBye();
            case "PONG" -> pong();
            default -> commandError();
        }
    }

    private void commandFileTransferAnswer(Map<String, String> message) {
        message.forEach((key, data) -> {
            System.out.println(key + " / " + data);
        });
        String answer = message.get("answer");
        String sender = message.get("username");
        System.out.println("ServerSideClient/commandFileTransferAnswer -> answer: " + answer + ", username: " + sender);


        ServerSideClient senderReceiver = Server.getInstance().getUser(sender);
        if (senderReceiver == null) {
            this.sendToClient("FILE_TRF_ANSWER", new MessageError("??????"));
            return;
        }

        MessageFileTrfAnswer mfta = new MessageFileTrfAnswer(username, String.valueOf(answer));

        senderReceiver.sendToClient("FILE_TRF_ANSWER", mfta);
    }

    private void commandFileTransfer(Map<String, String> message) {
        String receiver = message.get("username");
        String fileName = message.get("fileName");
        System.out.println(receiver + " " + fileName);

        ServerSideClient receiverClient = Server.getInstance().getUser(receiver);
        if (receiverClient == null) {
            this.sendToClient("FILE_TRF_RESP", new MessageError("3001"));
            return;
        }
        if (!isLoggedIn) {
            this.sendToClient("FILE_TRF_RESP", new MessageError("3000"));
            return;
        }
        if (receiverClient.username.equals(username)) {
            this.sendToClient("FILE_TRF_RESP", new MessageError("3003"));
            return;
        }

        MessageFileTransfer mft = new MessageFileTransfer(username, fileName);
        FIleTransfer fIleTransfer = new FIleTransfer(Server.getInstance().getFileTransferSocket());
        fIleTransfer.start();

        receiverClient.sendToClient("FILE_TRF", mft);

    }

    private void pong() {
        System.out.println("Received Pong!");
        if (pingPongInteraction != null) {
            System.out.println("Notifying that Pong was received.");
            pingPongInteraction.receivedPong();
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
        } else if (Server.getInstance().getUser(username) != null) {
            finalMessage = new MessageError("5002");
        } else {
            finalMessage = new MessageGoodStatus();
            isLoggedIn = true;
            this.username = username;
            if (PERFORM_PING_PONG) {
                pingPongInteraction = new PingPongInteraction(this);
                Thread pingPongThread = new Thread(pingPongInteraction);
                pingPongThread.start();
            }
        }

        sendToClient(responseCommand, finalMessage);
    }

    private void commandBroadcastReq(Map<String, String> message) {
        String messageS = message.get("message");
        String responseCommand = "BROADCAST_RESP";


        if (!isLoggedIn) {
            sendToClient(responseCommand, new MessageError("6000"));
            return;
        }
        sendToClient(responseCommand, new MessageGoodStatus());

        JsonMessage messageToBroadcast = new MessageBroadcast(this.username, messageS);
        Server.getInstance().broadcastAllIgnoreSender("BROADCAST", messageToBroadcast, this.username);
    }

    private void commandPrivateSend(Map<String, String> message) {
        String receiver = message.get("username");
        String messageS = message.get("message");
        String responseCommand = "PRIVATE_RESP";

        JsonMessage finalMessage;
        if (!isLoggedIn) {
            finalMessage = new MessageError("1000");
        } else if (messageS.isBlank()) {
            finalMessage = new MessageError("1001");
        } else if (Server.getInstance().getUser(receiver) == null) {
            finalMessage = new MessageError("1002");
        } else if (this.username.equals(receiver)) {
            finalMessage = new MessageError("1003");
        } else {
            finalMessage = new MessageGoodStatus();
        }
        // Handle sender answer.
        sendToClient(responseCommand, finalMessage);

        // Handle message receiver stage.
        Server.getInstance().broadcastTo("PRIVATE_RECEIVE", new PrivateReceiveMessage(this.username, messageS)
                , Server.getInstance().getUser(receiver));

    }

    private void commandGGCreate(Map<String, String> message) {
        String code = "GG_CREATE_RESP";
        JsonMessage messageToSend;
        if (Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8001");
        } else {
            messageToSend = new MessageGoodStatus();
            Server.getInstance().setGameCreated(true);
        }
        sendToClient(code, messageToSend);
        Server.getInstance().broadcastAllIgnoreSender("GG_INVITE", new MessageInvite(), this.username);
        Server.guessGame = new GuessGame(this);
        Server.guessGame.start();

    }

    private void commandGGJoin(Map<String, String> message) {
        String code = "GG_JOIN_RESP";
        JsonMessage messageToSend;
        if (!Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8002");
        } else if (hasJoinedGame) {
            messageToSend = new MessageError("8003");
        } else {
            messageToSend = new MessageGoodStatus();
        }
        sendToClient(code, messageToSend);
        Server.guessGame.addGamer(this);
    }

    private void commandGG_Guess(Map<String, String> message) {
        String code = "GG_GUESS_RESP";
        JsonMessage messageToSend;
        try {
            int guess = Integer.parseInt(message.get("guess"));
            if (guess < 1 || guess > 50) {
                messageToSend = new MessageError("8006");
            } else {
                messageToSend = new MessageGuess(Server.guessGame.compareNumber(guess, this));
            }
        } catch (NumberFormatException e) {
            messageToSend = new MessageError("8005");
        }
        sendToClient(code, messageToSend);
    }

    private void commandGGLeave(Map<String, String> message) {

    }

    private void commandBye() {
        if (pingPongInteraction != null) {
            pingPongInteraction.disconnect();
        }
    }

    private void commandError() {

    }

    public String getUsername() {
        return username;
    }

    public void sendToClient(String code, JsonMessage message) {
        try {
            System.out.println("Sending to client: " + code + " " + message.mapToJson());
            writer.println(code.toUpperCase() + message.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToClient(String code) {
        System.out.println("Sending to client: " + code);
        writer.println(code);
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