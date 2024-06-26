package Server;

import Shared.Headers.ByeHeader;
import Shared.Messages.JsonMessage;
import Shared.Messages.MessageLogin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Server {
    public final boolean PERFORM_PING_PONG = true;
    private static Server instance;

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    private final ServerSocket serverSocket;
    private final Set<ServerSideClient> clients = new HashSet<>();
    private boolean isGameCreated = false;
    public static GuessGame guessGame;

    public static void main(String[] args) {
        new Server().start();
    }

    private Server() {
        instance = this;
        try {
            this.serverSocket = new ServerSocket(1337);
            FileTransfer.getInstance().start();
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
        broadcastAll(ByeHeader.LEFT, new MessageLogin(serverSideClient.getUsername()));
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

    public ServerSideClient getUser(String username) {
        for (ServerSideClient serverSideClient : clients) {
            if (serverSideClient.getUsername() == null) continue;
            if (serverSideClient.getUsername().equals(username)) {
                return serverSideClient;
            }
        }
        return null;
    }

    public byte[] getUserPublicKey(String username) {
        ServerSideClient user = getUser(username);
        if (user == null) return null;
        if (user.getPublicKey() == null) return null;
        return user.getPublicKey().getEncoded();
    }

    @SuppressWarnings("rawtypes")
    public void broadcastAllIgnoreSender(Enum header, JsonMessage message, String sender) {
        broadcastAll(header, message, sender);
    }

    @SuppressWarnings("rawtypes")
    public void broadcastAll(Enum header, JsonMessage message) {
        broadcastAll(header, message, "");
    }

    @SuppressWarnings("rawtypes")
    private void broadcastAll(Enum header, JsonMessage message, String username) {
        for (ServerSideClient client : clients) {
            if (client.getUsername() == null || client.getUsername().equals(username)) {
                continue;
            }
            client.sendToClient(header, message);
        }
    }

    @SuppressWarnings("rawtypes")
    public void broadcastTo(Enum header, JsonMessage message, Set<ServerSideClient> receivers) {
        for (ServerSideClient client : receivers) {
            broadcastTo(header, message, client);
        }
    }

    @SuppressWarnings("rawtypes")
    public void broadcastTo(Enum header, JsonMessage message, ServerSideClient receiver) {
        System.out.println("Broadcasting " + message);
        receiver.sendToClient(header, message);
    }

    @SuppressWarnings("rawtypes")
    public void broadcastTo(Enum header, JsonMessage message, String receiver) {
        System.out.println("Broadcasting " + message);
        ServerSideClient receiverClient = getUser(receiver);
        if (receiverClient == null) {
            return;
        }
        receiverClient.sendToClient(header, message);
    }

    public List<String> getClients() {
        return clients.stream().map(serverSideClient -> serverSideClient.getUsername())
                .filter(Objects::nonNull)
                .toList();
    }
}