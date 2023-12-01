package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private PrintWriter writer;
    private BufferedReader reader;
    private final ServerSocket serverSocket;
    public static void main(String[] args) {
        new Server().start();
    }

    public Server() {
        try {
            this.serverSocket = new ServerSocket(1337);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeStreams();
        }
    }

    private void closeStreams() {
        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
