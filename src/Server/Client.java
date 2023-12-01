package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Client implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;

    public Client(PrintWriter writer, BufferedReader reader) {
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                writer.println("Server: Message received - " + inputLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
