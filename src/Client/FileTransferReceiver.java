package Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileTransferReceiver extends Thread {
    private Socket socket;
    private String pathFolder = "ReceivedFiles/";
    private String pathFile;

    public FileTransferReceiver(Socket socket, String fileName) {
        this.socket = socket;
        this.pathFile = pathFolder + fileName;
    }

    @Override
    public void run() {
        try {
            InputStream receiverInputStream = socket.getInputStream();
            System.out.println(pathFile);
            FileOutputStream fileOutputStream = new FileOutputStream(pathFile);
            receiverInputStream.transferTo(fileOutputStream);

            receiverInputStream.close();
            fileOutputStream.close();
            socket.close();

            System.out.println("File received and saved at: " + pathFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
