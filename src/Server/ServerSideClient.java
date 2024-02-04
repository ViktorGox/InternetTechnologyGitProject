package Server;

import Shared.ClientCommand;
import Shared.EncryptionUtils;
import Shared.Headers.*;
import Shared.MessageFactory;
import Shared.Messages.Broadcast.MessageBroadcast;
import Shared.Messages.Broadcast.MessageBroadcastRequest;
import Shared.Messages.Encryption.*;
import Shared.Messages.*;
import Shared.Messages.PrivateMessage.MessagePrivateReceive;
import Shared.Messages.PrivateMessage.MessagePrivateSend;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.UUID;

import static Client.Client.DISPLAY_RAW_DEBUG;

public class ServerSideClient implements Runnable {
    public static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{3,14}$";
    private final PrintWriter writer;
    private final BufferedReader reader;
    private PublicKey publicKey;
    private String username = null;
    private PingPongInteraction pingPongInteraction;
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
        writer.println("WELCOME");
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                ClientCommand command = new ClientCommand(inputLine);
                determineAction(command);
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

    public void determineAction(ClientCommand clientCommand) {
        if(DISPLAY_RAW_DEBUG) System.out.println(clientCommand);
        if(clientCommand.getCommandAsEnum() == null) {
            sendToClient(OtherHeader.UNKNOWN_COMMAND);
            return;
        }
        JsonMessage createdMessage = MessageFactory.convertToMessageClass(clientCommand);
        // Parse or unknown command error handling.
        if(createdMessage instanceof MessageError) {
            if(((MessageError) createdMessage).getCode().equals("0")) {
                sendToClient(OtherHeader.PARSE_ERROR);
            }
            if(((MessageError) createdMessage).getCode().equals("1")) {
                sendToClient(OtherHeader.UNKNOWN_COMMAND);
            }
            return;
        }

        if(DISPLAY_RAW_DEBUG) System.out.println("Handling: " + createdMessage);
        switch (clientCommand.getCommand()) {
            case "LOGIN" -> commandLogIn(createdMessage);
            case "BROADCAST_REQ" -> commandBroadcastReq(createdMessage);
            case "PRIVATE_SEND" -> commandPrivateSend(createdMessage);
            case "GG_CREATE" -> commandGGCreate();
            case "GG_JOIN" -> commandGGJoin(createdMessage);
            case "GG_GUESS" -> commandGG_Guess(createdMessage);
            case "GG_LEAVE" -> commandGGLeave(createdMessage);
            case "FILE_TRF" -> commandFileTransfer(createdMessage);
            case "FILE_TRF_ANSWER" -> commandFileTransferAnswer(createdMessage);
            case "REQ_PUBLIC_KEY" -> commandReqPublicKey(createdMessage);
            case "REQ_PUBLIC_KEY_RESP" -> commandReqPublicKeyResp(createdMessage);
            case "SESSION_KEY_CREATE" -> commandSessionKeyCreate(createdMessage);
            case "SESSION_KEY_CREATE_RESP" -> commandSessionKeyCreateResp(createdMessage);
            case "ENC_PRIVATE_SEND" -> commandEncPrivateSend(createdMessage);
            case "USER_LIST" -> commandUserList();
            case "BYE" -> commandBye();
            case "PONG" -> pong();
            default -> commandError();
        }
    }

    private void commandSessionKeyCreateResp(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageSessionKeyCreateResp)) System.out.println("SessionKeyCreateResp conversion failed");
        MessageSessionKeyCreateResp message = (MessageSessionKeyCreateResp) jsonMessage;

        MessageSessionKeyCreateResp messageJson = new MessageSessionKeyCreateResp("OK", this.username);
        Server.getInstance().broadcastTo(EncryptedPrivateHeader.SESSION_KEY_CREATE_RESP, messageJson, message.getUsername());
    }

    private void commandEncPrivateSend(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageEncPrivateSend)) System.out.println("EncPrivateSend conversion failed");
        MessageEncPrivateSend message = (MessageEncPrivateSend) jsonMessage;


        MessageEncPrivateSend messageBroadcast = new MessageEncPrivateSend(this.username, message.getMessage());
        Server.getInstance().broadcastTo(EncryptedPrivateHeader.ENC_PRIVATE_RECEIVE, messageBroadcast, message.getUsername());
    }

    private void commandSessionKeyCreate(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageSessionKeyCreate)) System.out.println("SessionKeyCreate conversion failed");
        MessageSessionKeyCreate message = (MessageSessionKeyCreate) jsonMessage;

        MessageSessionKeyCreate returnMessage = new MessageSessionKeyCreate(message.getSessionKey(), this.username);
        Server.getInstance().broadcastTo(EncryptedPrivateHeader.SESSION_KEY_CREATE, returnMessage, message.getUsername());
    }

    /**
     * Received public key from client.
     */
    private void commandReqPublicKeyResp(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageReqPublicKeyResp)) System.out.println("ReqPublicKeyResp conversion failed");
        MessageReqPublicKeyResp message = (MessageReqPublicKeyResp) jsonMessage;

        this.publicKey = EncryptionUtils.stringByteArrayToPublicKey(message.getPublicKey());

        JsonMessage mrpkr = new MessageReqPublicKeyResp(message.getPublicKey(), this.username);
        Server.getInstance().broadcastTo(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP, mrpkr, message.getUsername());
    }

    private void commandReqPublicKey(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageReqPublicKey)) System.out.println("ReqPublicKey conversion failed.");
        MessageReqPublicKey message = (MessageReqPublicKey) jsonMessage;

        if (username == null) {
            sendToClient(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP,
                    new MessageReqPublicKeyRespError("3000", message.getUsername()));
            return;
        }
        if (username.equals(message.getUsername())) {
            sendToClient(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP,
                    new MessageReqPublicKeyRespError("3002", message.getUsername()));
            return;
        }
        if (Server.getInstance().getUser(message.getUsername()) == null) {
            sendToClient(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP,
                    new MessageReqPublicKeyRespError("3001", message.getUsername()));
            return;
        }

        byte[] publicKeyEncoded = Server.getInstance().getUserPublicKey(message.getUsername());
        if (publicKeyEncoded != null) {
            sendToClient(EncryptedPrivateHeader.REQ_PUBLIC_KEY_RESP,
                    new MessageReqPublicKeyResp(publicKeyEncoded, message.getUsername()));
        } else {
            Server.getInstance().broadcastTo(EncryptedPrivateHeader.REQ_PUBLIC_KEY
                    , new MessageReqPublicKey(this.username)
                    , message.getUsername());
        }
    }

    private void commandUserList() {
        JsonMessage finalMessage;
        if(!isLoggedIn){
            finalMessage = new MessageError("2000");
        } else {
            finalMessage = new MessageUserList(Server.getInstance().getClients());
        }
        sendToClient(UserListHeader.USER_LIST_RESP, finalMessage);
    }

    private void commandFileTransferAnswer(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageFileTrfAnswer)) System.out.println("FileTransferAsnwer conversion failed");
        MessageFileTrfAnswer message = (MessageFileTrfAnswer) jsonMessage;

        String answer = message.getAnswer();
        String sender = message.getUsername();
        UUID uuid = message.getUuid();

        ServerSideClient senderReceiver = Server.getInstance().getUser(sender);
        if (senderReceiver == null) {
            //TODO: add some error?
            this.sendToClient(FileTransferHeader.FILE_TRF_ANSWER, new MessageError("??????"));
            return;
        }

        MessageFileTrfAnswer mfta = new MessageFileTrfAnswer(username, String.valueOf(answer), uuid);

        senderReceiver.sendToClient(FileTransferHeader.FILE_TRF_ANSWER, mfta);
    }

    private void commandFileTransfer(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageFileTransfer)) System.out.println("FileTransfer conversion failed.");
        MessageFileTransfer message = (MessageFileTransfer) jsonMessage;

        String receiver = message.getUsername();
        String fileName = message.getFileName();
        System.out.println(receiver + " " + fileName);

        ServerSideClient receiverClient = Server.getInstance().getUser(receiver);
        if (receiverClient == null) {
            this.sendToClient(FileTransferHeader.FILE_TRF_RESP, new MessageError("3001"));
            return;
        }
        if (!isLoggedIn) {
            this.sendToClient(FileTransferHeader.FILE_TRF_RESP, new MessageError("3000"));
            return;
        }
        if (receiverClient.username.equals(username)) {
            this.sendToClient(FileTransferHeader.FILE_TRF_RESP, new MessageError("3003"));
            return;
        }

        MessageFileTransfer mft = new MessageFileTransfer(username, fileName);

        receiverClient.sendToClient(FileTransferHeader.FILE_TRF, mft);

    }

    private void pong() {
        System.out.println("Received Pong!");
        if (pingPongInteraction != null) {
            System.out.println("Notifying that Pong was received.");
            if(pingPongInteraction.pingState()) {
                if(DISPLAY_RAW_DEBUG) System.out.println("PONG WIHTOUT PING");
                sendToClient(PingPongHeader.PONG_ERROR, new MessageStatuslessError("8000"));
            }
            pingPongInteraction.receivedPong();
        } else {
            sendToClient(PingPongHeader.PONG_ERROR, new MessageStatuslessError("8000"));
        }
    }

    private void commandLogIn(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageLogin)) System.out.println("ServerSideClient from command Log in says: " +
                "??????? fr fr");
        MessageLogin message = (MessageLogin) jsonMessage;
        JsonMessage finalMessage;
        if(message == null){
            finalMessage = new MessageError("5001");
        } else {
            String username = message.getUsername();
            if (isLoggedIn) {
                finalMessage = new MessageError("5002");
            } else if (!username.matches(VALID_USERNAME_REGEX)) {
                finalMessage = new MessageError("5001");
            } else if (Server.getInstance().getUser(username) != null) {
                finalMessage = new MessageError("5000");
            } else {
                finalMessage = new MessageGoodStatus();
                isLoggedIn = true;
                this.username = username;
                Server.getInstance().broadcastAllIgnoreSender(LoginHeader.JOINED,
                        new MessageJoined(username), username);
                if (Server.getInstance().PERFORM_PING_PONG) {
                    pingPongInteraction = new PingPongInteraction(this);
                    Thread pingPongThread = new Thread(pingPongInteraction);
                    pingPongThread.start();
                }
            }
        }

        sendToClient(LoginHeader.LOGIN_RESP, finalMessage);
    }

    private void commandPrivateSend(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessagePrivateSend))
            System.out.println("ServerSideClient from Private Send says: ???????");
        MessagePrivateSend message = (MessagePrivateSend) jsonMessage;

        String receiver = message.getUsername();
        String messageS = message.getMessage();

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

            Server.getInstance().broadcastTo(PrivateMessageHeader.PRIVATE_RECEIVE
                    , new MessagePrivateReceive(this.username, messageS)
                    , Server.getInstance().getUser(receiver));
        }
        // Handle sender answer.
        sendToClient(PrivateMessageHeader.PRIVATE_RESP, finalMessage);
    }

    private void commandBroadcastReq(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageBroadcastRequest)) System.out.println("commandBroadcastReq conversio nfailed");
        MessageBroadcastRequest message = (MessageBroadcastRequest) jsonMessage;
        String messageS = message.getMessage();

        if (!isLoggedIn) {
            sendToClient(BroadcastHeader.BROADCAST_REQ, new MessageError("6000"));
            return;
        }
        sendToClient(BroadcastHeader.BROADCAST_RESP, new MessageGoodStatus());

        JsonMessage messageToBroadcast = new MessageBroadcast(this.username, messageS);
        Server.getInstance().broadcastAllIgnoreSender(BroadcastHeader.BROADCAST, messageToBroadcast, this.username);
    }

    private void commandGGCreate() {
        JsonMessage messageToSend;
        if (!isLoggedIn) {
            messageToSend = new MessageError("8010");
        } else if (Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8001");
        } else {
            messageToSend = new MessageGoodStatus();
            Server.getInstance().setGameCreated(true);
            Server.getInstance().broadcastAllIgnoreSender(GuessingGameHeader.GG_INVITATION, new MessageInvite(username), this.username);
            Server.guessGame = new GuessGame(this);
            Server.guessGame.start();
        }
        sendToClient(GuessingGameHeader.GG_CREATE_RESP, messageToSend);

    }

    private void commandGGJoin(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageJoined))
            System.out.println("commandGGJoin failed");
        MessageJoined message = (MessageJoined) jsonMessage;

        JsonMessage messageToSend;
        if (!isLoggedIn) {
            messageToSend = new MessageError("8010");
        } else if (!Server.getInstance().isGameCreated()) {
            messageToSend = new MessageError("8002");
        } else if (hasJoinedGame) {
            messageToSend = new MessageError("8003");
        } else {
            messageToSend = new MessageGoodStatus();
            Server.guessGame.addGamer(this);
        }
        sendToClient(GuessingGameHeader.GG_JOIN_RESP, messageToSend);
    }

    private void commandGG_Guess(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageGuess))
            System.out.println("commandGG_Guess failed");
        MessageGuess message = (MessageGuess) jsonMessage;

        JsonMessage messageToSend;
        if (!isLoggedIn) {
            messageToSend = new MessageError("8010");
        } else {
            try {
                int guess = Integer.parseInt(message.getGuess());
                if (!Server.getInstance().isGameCreated()) {
                    messageToSend = new MessageError("8008");
                } else if (guess < 1 || guess > 50) {
                    messageToSend = new MessageError("8006");
                } else {
                    messageToSend = new MessageGuess(Integer.toString(Server.guessGame.compareNumber(guess, this)));
                }
            } catch (NumberFormatException e) {
                messageToSend = new MessageError("8005");
            }
        }
        sendToClient(GuessingGameHeader.GG_GUESS_RESP, messageToSend);
    }

    private void commandGGLeave(JsonMessage jsonMessage) {

    }

    private void commandBye() {
        if (pingPongInteraction != null) {
            pingPongInteraction.disconnect();
        }
        Server.getInstance().removeClient(this);
    }

    private void commandError() {

    }

    public String getUsername() {
        return username;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }


    public void sendToClient(Enum header, JsonMessage message) {
        try {
            System.out.println("Sending to client: " + header + " " + message.mapToJson());
            writer.println(header + " " + message.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToClient(Enum header) {
        System.out.println("Sending to client: " + header);
        writer.println(header);
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