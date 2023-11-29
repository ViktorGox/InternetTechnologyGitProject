package Client;

import java.io.PrintWriter;
import java.util.Scanner;

public class UserInput implements Runnable {
    private Scanner s;
    private final PrintWriter writer;
    private final Client realtedClient;
    private final String menu = """
            Menu:

            1: Log in
            2: Do y
            3: Do z
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
        while (!c.equals("q")) {
            switch (c) {
                case "?" -> System.out.println(menu);
                case "1" -> logIn();
                case "2" -> action2();
                case "3" -> action3();
                case "4" -> action4();
                case "5" -> action5();
            }
            c = s.nextLine().toLowerCase();
        }
        realtedClient.closeInputFromServer();
    }

    public void logIn() {
        System.out.println("Enter username: ");
        String username = s.nextLine().toLowerCase();

        writer.println("LOGIN {\"username\": \"" +username +"\"}");
    }

    public void action2() {

    }

    public void action3() {

    }

    public void action4() {

    }

    public void action5() {

    }
}
