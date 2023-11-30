package Client;

import Messages.MessageBroadcast;
import Messages.MessageLogin;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.PrintWriter;
import java.util.Scanner;

public class UserInput implements Runnable {
    private Scanner s;
    private final PrintWriter writer;
    private final Client realtedClient;
    private String username;
    private boolean terminate = false;
    private final String menu = """
            Menu:

            1: Log in
            2: Broadcast a message
            3: Logout
            4: Do w.
            5: Do v
            ?: This menu
            Q: Quit
            """;

    public UserInput(PrintWriter writer, Client relatedClient) {
        this.writer = writer;
        this.realtedClient = relatedClient;
    }

    @Override
    public void run() {
        System.out.println(menu);
        s = new Scanner(System.in);
        String c = s.nextLine().toLowerCase();
        while (!c.equals("q") && !terminate) {
            switch (c) {
                case "?" -> System.out.println(menu);
                case "1" -> logIn();
                case "2" -> broadcastMessage();
                case "3" -> logout();
                case "4" -> action4();
                case "5" -> action5();
            }
            if(!terminate) {
                c = s.nextLine().toLowerCase();
            }
        }
        System.out.println("EXITED");
        realtedClient.closeInputFromServer();
    }

    public void logIn() {
        System.out.println("Enter username: ");
        username = s.nextLine().toLowerCase();
        MessageLogin messageLogin = new MessageLogin(username);
        try {
            writer.println("LOGIN " + messageLogin.mapToJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage() {
        System.out.println("Enter your message: ");
        String message = s.nextLine();
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

    public void action4() {

    }

    public void action5() {

    }
}
