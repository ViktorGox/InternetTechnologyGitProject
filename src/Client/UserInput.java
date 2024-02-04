package Client;

import Shared.Headers.*;
import Shared.Messages.Broadcast.MessageBroadcastRequest;
import Shared.Messages.Encryption.MessageEncPrivateSend;
import Shared.Messages.Encryption.MessageReqPublicKey;
import Shared.Messages.*;
import Shared.Messages.PrivateMessage.MessagePrivateSend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import static Client.Client.DISPLAY_RAW_DEBUG;
import static Server.ServerSideClient.VALID_USERNAME_REGEX;

public class UserInput implements Runnable {
    protected Scanner inputScanner;
    protected final PrintWriter writer;
    protected final BufferedReader reader;
    private final Client client;
    private String username;
    private boolean terminate = false;
    private volatile boolean response = false;
    private boolean joinedGame = false;
    private volatile boolean ggStarted = false;
    private ArrayList<OnClientExited> onClientExitedListeners = new ArrayList<>();
    private FileTransferSender fileTransferSender;
    private String fileName;
    private final String menu = """
            Menu:

            1: Log in
            2: Broadcast a message
            3: Send private message
            4: Send encrypted message
            5: File Transfer
            6: Create a Guessing Game
            7: Join a Guessing Game
            8: User List
            X: Logout
            ?: This menu
            Q: Quit
            """;

    private String fileTransferReceiver;

    public UserInput(PrintWriter writer, BufferedReader reader, Client client) {
        this.writer = writer;
        this.reader = reader;
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(menu);
        inputScanner = new Scanner(System.in);
        String line = inputScanner.nextLine().toLowerCase();
        while (!line.equals("q") && !terminate) {
            switch (line) {
                case "?" -> System.out.println(menu);
                case "1" -> logIn();
                case "2" -> broadcastMessage();
                case "3" -> privateMessage();
                case "4" -> encryptedPrivateMessage();
                case "5" -> fileTransfer();
                case "6" -> guessGame();
                case "7" -> joinGame();
                case "8" -> userList();
                case "0" -> logout();
            }
            if (!terminate) {
                line = inputScanner.nextLine().toLowerCase();
                if (line.equals("a") || line.equals("r")) handleFileTransferAnswer(line);
            }
        }
        System.out.println("EXITED");
        fireEvent();
    }

    private void userList() {
        client.send(UserListHeader.USER_LIST);
    }

    public void logIn() {
        System.out.println("Enter username: ");
        username = inputScanner.nextLine();
        MessageLogin messageLogin = new MessageLogin(username);
        client.send(LoginHeader.LOGIN, messageLogin);
    }

    public void broadcastMessage() {
        System.out.println("Enter your message: ");
        String message = inputScanner.nextLine();
        MessageBroadcastRequest messageBroadcastRequest = new MessageBroadcastRequest(message);
        client.send(BroadcastHeader.BROADCAST_REQ, messageBroadcastRequest);
    }

    public void logout() {
        client.send(ByeHeader.BYE);
        terminate = true;
    }

    public void fileTransfer() {
        System.out.println("Receiver username: ");
        String receiver = inputScanner.nextLine();
        System.out.println("Enter a File name (Example: test.txt):");
        fileName = inputScanner.nextLine();
        File file = new File("FilesToSend/" + fileName);
        while (!file.exists()) {
            System.out.println("This file is not in FilesToSend folder, Please Try Again");
            fileName = inputScanner.nextLine();
            file = new File("FilesToSend/" + fileName);
        }

        MessageFileTransfer messageFileTransfer = new MessageFileTransfer(receiver, fileName);
        client.send(FileTransferHeader.FILE_TRF, messageFileTransfer);
    }

    public void startFileTransferSend(UUID uuid) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("127.0.0.1", 1338);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileTransferSender = new FileTransferSender(clientSocket, "FilesToSend/" + fileName, uuid);
        fileTransferSender.start();
        System.out.println("Client accepted the file transfer");
    }

    private void guessGame() {
        writer.println("GG_CREATE");
        waitForGameResponse();
        if (joinedGame) {
            waitForGameStart();
            makeGuess();
        }
    }

    private void waitForGameStart() {
        System.out.println("Waiting for the game to start, hang on tight!");
        while (!ggStarted) {
            Thread.onSpinWait();
        }
        ggStarted = false;
    }

    private void waitForGameResponse() {
        while (!response) {
            Thread.onSpinWait();
        }
        response = false;
    }

    private void makeGuess() {
        while (joinedGame) {
            System.out.println("If you want to quit enter q");
            System.out.println("Make a guess between 1 and 50");
            String input = inputScanner.nextLine().toLowerCase();
            if (input.equals("q")) {
                joinedGame = false;
            } else {
                MessageGuess messageGuess = new MessageGuess(input);
                client.send(GuessingGameHeader.GG_GUESS, messageGuess);
                waitForGameResponse();
            }
        }
    }

    private void joinGame() {
        writer.println("GG_JOIN");
        waitForGameResponse();
        if (joinedGame) {
            waitForGameStart();
            makeGuess();
        }
    }

    protected void closeStreams() {
        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void privateMessage() {
        System.out.println("Enter receiver:");
        String receiver = inputScanner.nextLine();
        System.out.println("Enter your message: ");
        String message = inputScanner.nextLine();
        MessagePrivateSend messageBroadcast = new MessagePrivateSend(receiver, message);
        client.send(PrivateMessageHeader.PRIVATE_SEND, messageBroadcast);
    }

    private void encryptedPrivateMessage() {
        if (!client.isLoggedIn()) {
            System.out.println("You must be logged in to send an encrypted message: ");
            return;
        }

        System.out.println("Enter receiver:");
        String receiver = inputScanner.nextLine();
        System.out.println("Enter your message: ");
        String message = inputScanner.nextLine();

        byte[] sessionKey = client.getSessionKey(receiver);

        if (sessionKey == null) {
            handleSessionKeyHandShake(receiver);
            client.addToWaitingList(receiver, message);
            return;
        }

        String encryptedMessage = client.getEncryptionHandler().encryptWithSessionKey(message, sessionKey);

        MessageEncPrivateSend messageBroadcast = new MessageEncPrivateSend(receiver, encryptedMessage);
        client.send(EncryptedPrivateHeader.ENC_PRIVATE_SEND, messageBroadcast);
    }

    private void handleSessionKeyHandShake(String receiver) {
        if (!receiver.matches(VALID_USERNAME_REGEX)) {
            System.out.println("That is not a valid username.");
            return;
        }
        client.send(EncryptedPrivateHeader.REQ_PUBLIC_KEY, new MessageReqPublicKey(receiver));
    }

    protected void handleFireTransfer(JsonMessage jsonMessage) {
        if (!(jsonMessage instanceof MessageFileTransfer)) System.out.println("handleFireTransfer conversion failed");
        MessageFileTransfer message = (MessageFileTransfer) jsonMessage;

        fileName = message.getFileName();
        String username = message.getUsername();
        System.out.println(username + " wants to send you a file: " + fileName);
        System.out.println();
        System.out.println("""
                A -> Accept
                R -> Reject
                """);

        fileTransferReceiver = username;
    }

    private void handleFileTransferAnswer(String input) {
        if (DISPLAY_RAW_DEBUG) System.out.println("Handling file transfer answer.");
        if (DISPLAY_RAW_DEBUG) System.out.println("Line -> " + fileTransferReceiver);
        switch (input.toUpperCase()) {
            case "A" -> answerFileTransfer(fileTransferReceiver, true);
            case "R" -> answerFileTransfer(fileTransferReceiver, false);
        }
    }

    public void answerFileTransfer(String sender, boolean answer) {
        try {
            UUID uuid = UUID.randomUUID();
            client.send(FileTransferHeader.FILE_TRF_ANSWER, new MessageFileTrfAnswer(sender, String.valueOf(answer), uuid));
            if (answer) {
                Socket clientSocket = new Socket("127.0.0.1", 1338);
                FileTransferReceiver fileTransferReceiver = new FileTransferReceiver(clientSocket, fileName, uuid);
                fileTransferReceiver.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addListener(OnClientExited listener) {
        onClientExitedListeners.add(listener);
    }

    public void fireEvent() {
        for (OnClientExited listener : onClientExitedListeners) {
            listener.onClientExited();
        }
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public void setGgStarted(boolean ggStarted) {
        this.ggStarted = ggStarted;
    }

    public void setJoinedGame(boolean joinedGame) {
        this.joinedGame = joinedGame;
    }
}