package Client;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class FileTransferSender extends Thread {
    private Socket socket;
    private String path;
    private UUID uuid;

    public FileTransferSender(Socket socket, String path, UUID uuid) {
        this.socket = socket;
        this.path = path;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            socket.getOutputStream().write(uuid.toString().getBytes());
            socket.getOutputStream().flush();
            socket.getInputStream().read();
            System.out.println("STARTING TRANSFER NOW");
            FileInputStream fileInputStream = new FileInputStream(path);
            OutputStream outputStream = socket.getOutputStream();
            fileInputStream.transferTo(outputStream);
            socket.close();
            fileInputStream.close();
            System.out.println("STOPPED TRANSFER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
