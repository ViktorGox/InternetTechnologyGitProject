package Server;

import Messages.JsonMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static Server instance;

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    private final ServerSocket serverSocket;
    private final HashSet<ServerSideClient> clients = new HashSet<>();
    private boolean isGameCreated = false;

    public static void main(String[] args) {
        new Server().start();
    }

    private Server() {
        instance = this;
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
            ServerSideClient newServerSideClient = new ServerSideClient(socket);
            addClient(newServerSideClient);

            Thread newClientThread = new Thread(newServerSideClient);
            newClientThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void removeClient(ServerSideClient serverSideClient) {
        clients.remove(serverSideClient);
        printClients();
    }

    public synchronized void addClient(ServerSideClient serverSideClient) {
        clients.add(serverSideClient);
        printClients();
    }

    private void printClients() {
        System.out.println("============================");
        System.out.println("    Clients connected: " + clients.size());
        for (ServerSideClient client : clients) {
            System.out.println("    " + client);
        }
        System.out.println("============================");
    }

    public boolean isGameCreated() {
        return isGameCreated;
    }

    public void setGameCreated(boolean gameCreated) {
        isGameCreated = gameCreated;
    }

    public boolean containsUser(String username) {
        for (ServerSideClient serverSideClient : clients) {
            if (serverSideClient.getUsername() == null) continue;
            if (serverSideClient.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastAllIgnoreSender(String code, JsonMessage message, String sender) {
        broadcastAll(code, message, sender);
    }

    public void broadcastAll(String code, JsonMessage message) {
        broadcastAll(code, message, "");
    }

    private void broadcastAll(String code, JsonMessage message, String username) {
        for (ServerSideClient client : clients) {
            if(client.getUsername().equals(username)) {
                continue;
            }
            client.sendToClient(code, message);
        }
    }

    public void broadcastTo(String code, JsonMessage message, Set<ServerSideClient> receivers) {
        for (ServerSideClient client : receivers) {
            client.sendToClient(code, message);
        }
    }
}