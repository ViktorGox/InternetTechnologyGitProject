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
            socket.getOutputStream().write("s".getBytes());
            socket.getOutputStream().flush();
            socket.getInputStream().read();
            FileInputStream fileInputStream = new FileInputStream(path);
            OutputStream outputStream = socket.getOutputStream();
            fileInputStream.transferTo(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
