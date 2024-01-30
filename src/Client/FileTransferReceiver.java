package Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileTransferReceiver extends Thread {
    private Socket socket;
    private String path = "C:/Receiver";

    public FileTransferReceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream receiverInputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            receiverInputStream.transferTo(fileOutputStream);

            receiverInputStream.close();
            fileOutputStream.close();
            socket.close();

            System.out.println("File received and saved at: " + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
