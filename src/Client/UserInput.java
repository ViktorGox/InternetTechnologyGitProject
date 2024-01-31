package Client;

import Shared.Headers.*;
import Shared.Messages.Broadcast.MessageBroadcastRequest;
import Shared.Messages.Encryption.MessageEncPrivateSend;
import Shared.Messages.Encryption.MessageReqPublicKey;
import Shared.Messages.*;
import Shared.Messages.PrivateMessage.MessagePrivateSend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static Client.Client.DISPLAY_RAW_DEBUG;
import static Server.ServerSideClient.VALID_USERNAME_REGEX;

public class UserInput implements Runnable {
    protected Scanner inputScanner;
    protected final PrintWriter writer;
    protected final BufferedReader reader;
    private final Client client;
    private String username;
    private boolean terminate = false;
    private ArrayList<OnClientExited> onClientExitedListeners = new ArrayList<>();
    private FileTransferSender fileTransferSender;
    private final String menu = """
            Menu:

            1: Log in
            2: Broadcast a message
            3: Send private message
            4: File Transfer
            5: Create a Guessing Game
            6: Join a Guessing Game
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
                case "X" -> logout();
            }
            if (!terminate) {
                line = inputScanner.nextLine().toLowerCase();
                if (line.equals("a") || line.equals("r")) handleFileTransferAnswer(line);
            }
        }
        System.out.println("EXITED");
        fireEvent();
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
        MessageFileTransfer messageFileTransfer = new MessageFileTransfer(receiver, "Some ass file name");
        try {
            client.send(FileTransferHeader.FILE_TRF, messageFileTransfer);
            Socket clientSocket = new Socket("127.0.0.1", 1338);
            fileTransferSender = new FileTransferSender(clientSocket, "C:/Sender/test.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startFileTransferSend() {
        fileTransferSender.start();
        System.out.println("Client accepted the file transfer");
    }

    public void guessGame() {
        client.send(GuessingGameHeader.GG_CREATE);
    }

    private void makeGuess() {
        System.out.println("Make a guess between 1 and 50");
        int guess = Integer.parseInt(inputScanner.nextLine());
        MessageGuess messageGuess = new MessageGuess(guess);
        client.send(GuessingGameHeader.GG_GUESS, messageGuess);
    }

    private void joinGame() {
        writer.println("GG_JOIN");
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
        System.out.println("Enter receiver:");
        String receiver = inputScanner.nextLine();
        System.out.println("Enter your message: ");
        String message = inputScanner.nextLine();

        String sessionKey = client.getSessionKey(receiver);

        if (sessionKey == null) {
            handleSessionKeyHandShake(receiver);
        }

        MessageEncPrivateSend messageBroadcast = new MessageEncPrivateSend(receiver, message);
        client.send(PrivateMessageHeader.PRIVATE_SEND, messageBroadcast);
    }

    private void handleSessionKeyHandShake(String receiver) {
        if (!receiver.matches(VALID_USERNAME_REGEX)) {
            System.out.println("That is not a valid username.");
            return;
        }
        client.send(EncryptedPrivateHeader.REQ_PUBLIC_KEY, new MessageReqPublicKey(receiver));
    }

    protected void handleFireTransfer(String received) {
        System.out.println("""
                A -> Accept
                R -> Reject
                """);
        Map<String, String> map = JsonMessageExtractor.extractInformation(received);
        if (map.get("username") == null) {
            throw new IllegalStateException("??? How did you receive this without having username in received???");
        }

        fileTransferReceiver = map.get("username");
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
            client.send(FileTransferHeader.FILE_TRF_ANSWER, new MessageFileTrfAnswer(sender, String.valueOf(answer)));
            if (answer) {
                Socket clientSocket = new Socket("127.0.0.1", 1338);
                FileTransferReceiver fileTransferReceiver = new FileTransferReceiver(clientSocket);
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
}