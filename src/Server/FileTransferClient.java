package Server;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class FileTransferClient extends Thread {
    private Socket socket;
    private volatile Socket otherClient;
    private String role;

    public FileTransferClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            waitForUUID();
            if(role.equals("s")) {
                transferFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForUUID() throws IOException {
        byte[] uuidBytes = new byte[36];
        byte[] roleBytes = new byte[1];

        InputStream inputStream = socket.getInputStream();
        inputStream.read(uuidBytes);
        inputStream.read(roleBytes);

        String receivedUUID = new String(uuidBytes);
        String receivedRole = new String(roleBytes);
        role = receivedRole;

        if (isValidUUID(receivedUUID)) {
            FileTransfer.getInstance().addClient(UUID.fromString(receivedUUID), this);
        }
    }

    private void transferFile() throws IOException {
        try {
            InputStream senderInputStream = socket.getInputStream();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            DigestInputStream digestInputStream = new DigestInputStream(senderInputStream, digest);

            while (otherClient == null) {
                Thread.onSpinWait();
            }
            OutputStream receiverOutputStream = otherClient.getOutputStream();
            byte[] checksum = digest.digest();
            receiverOutputStream.write(checksum);
            receiverOutputStream.flush();
            digestInputStream.transferTo(receiverOutputStream);
            receiverOutputStream.close();
            System.out.println("ENDED FILE TRANSFER");
        } catch (IOException | NoSuchAlgorithmException e) {
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

    public String getRole() {
        return role;
    }
}
