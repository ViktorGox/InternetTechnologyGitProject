package Client;

import Messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class UserInput implements Runnable {
    protected Scanner inputScanner;
    protected final PrintWriter writer;
    protected final BufferedReader reader;
    private String username;
    private boolean terminate = false;
    private ArrayList<OnClientExited> onClientExitedListeners = new ArrayList<>();
    private final String menu = """
            Menu:

            1: Log in
            2: Broadcast a message
            3: Logout
            4: File Transfer
            5: Do v
            ?: This menu
            Q: Quit
            """;

    private String fileTransferReceiver;

    public UserInput(PrintWriter writer, BufferedReader reader) {
        this.writer = writer;
        this.reader = reader;
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
                case "3" -> logout();
                case "4" -> fileTransfer();
                case "5" -> action5();
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
        try {
            writer.println("LOGIN " + messageLogin.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage() {
        System.out.println("Enter your message: ");
        String message = inputScanner.nextLine();
        MessageBroadcast messageBroadcast = new MessageBroadcast(username, message);
        try {
            writer.println("BROADCAST_REQ " + messageBroadcast.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void logout() {
        writer.println("BYE");
        terminate = true;
    }

    public void fileTransfer() {
        System.out.println("Receiver username: ");
        String receiver = inputScanner.nextLine();
        MessageFileTransfer messageFileTransfer = new MessageFileTransfer(receiver, "Some ass file name");
        try {
            writer.println("FILE_TRF " + messageFileTransfer.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void action5() {

    }

    protected void closeStreams() {
        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleFireTransfer(String received) {
        System.out.println("""
                A -> Accept
                R -> Reject
                """);
        Map<String, String> map = JsonMessageExtractor.extractInformation(received);
        if(map.get("username") == null) {
            throw new IllegalStateException("??? How did you receive this without having username in received???");
        }

        fileTransferReceiver = map.get("username");
    }

    private void handleFileTransferAnswer(String input) {
//        System.out.println("Handling file transfer answer.");
//        System.out.println("Line -> " + fileTransferReceiver);
        switch (input.toUpperCase()) {
            case "A" -> answerFileTransfer(fileTransferReceiver, true);
            case "R" -> answerFileTransfer(fileTransferReceiver, false);
        }
    }

    public void answerFileTransfer(String sender, boolean answer) {
        try {
//            System.out.println("Sending to server that I " + answer + " the question.");
            MessageFileTrfAnswer mfta = new MessageFileTrfAnswer(sender, String.valueOf(answer));
//            System.out.println("UserInput/answerFileTransfer -> Sending to server: " + mfta.mapToJson());
            writer.println("FILE_TRF_ANSWER " + mfta.mapToJson());
        } catch (JsonProcessingException e) {
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