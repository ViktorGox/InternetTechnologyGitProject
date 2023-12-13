package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ServerSideClient implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;

    private String username;

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

    public void DetermineCommand(String command) {
        switch (command) {
            case "LOGIN" -> System.out.println("a");
            case "BROADCAST_REQ"-> System.out.println("b");
            case "PRIVATE_SEND"-> System.out.println("g");
            case "GG_CREATE"-> System.out.println("d");
            case "GG_JOIN"-> System.out.println("e");
            case "GG_LEAVE"-> System.out.println("f");
            case "BYE"-> System.out.println("c");
            default -> System.out.println("ERROR");
        }
    }
}