package Client;

import java.io.*;
import java.net.Socket;

public class FileTransferSender extends Thread {
    private Socket socket;
    private String path;

    public FileTransferSender(Socket socket, String path) {
        this.socket = socket;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("ME".getBytes());
            fileInputStream.transferTo(outputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
