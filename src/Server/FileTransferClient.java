package Server;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class FileTransferClient extends Thread {
    private Socket socket;
    private volatile Socket otherClient;

    public FileTransferClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            waitForUUID();
            transferFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForUUID() throws IOException {
        System.out.println("Waiting for UUID");
        byte[] uuidBytes = new byte[36];

        InputStream inputStream = socket.getInputStream();
        inputStream.read(uuidBytes);

        String receivedUUID = new String(uuidBytes);

        if (isValidUUID(receivedUUID)) {
            System.out.println("Received valid UUID: " + receivedUUID);
            FileTransfer.getInstance().addClient(UUID.fromString(receivedUUID), this);
        } else {
            System.out.println("Received invalid UUID: " + receivedUUID);
        }
    }

    private void transferFile() {
        try {
            InputStream senderInputStream = socket.getInputStream();
            while (otherClient == null) {
                System.out.println("Waiting for otherClient");
                Thread.onSpinWait();
            }
            System.out.println("WAITING NO MORE");
            OutputStream receiverOutputStream = otherClient.getOutputStream();
            senderInputStream.transferTo(receiverOutputStream);
            System.out.println("ENDED");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void notifyClient() {
        try {
            socket.getOutputStream().write("START".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeStream() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOtherClient(Socket otherClient) {
        this.otherClient = otherClient;
    }

    public Socket getSocket() {
        return socket;
    }
}
