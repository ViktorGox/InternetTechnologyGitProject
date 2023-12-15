package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private final ServerSocket serverSocket;
    private final HashSet<ServerSideClient> clients = new HashSet<>();
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

            ServerSideClient newServerSideClient = new ServerSideClient(writer, reader);
            addClient(newServerSideClient);

            Thread newClientThread = new Thread(newServerSideClient);
            newClientThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addClient(ServerSideClient serverSideClient) {
        clients.add(serverSideClient);
        System.out.println(clients);
        System.out.println(clients.size());
    }
}