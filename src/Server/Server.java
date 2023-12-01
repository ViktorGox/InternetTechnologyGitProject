package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server {
    private final ServerSocket serverSocket;
    private final Map<String, Client> clients = new HashMap<>();

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
                handleClient(socket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Client newClient = new Client(writer, reader);
            Random random = new Random();
            clients.put(String.valueOf(random.nextInt(0,10)), newClient);

            Thread newClientThread = new Thread(newClient);
            newClientThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}