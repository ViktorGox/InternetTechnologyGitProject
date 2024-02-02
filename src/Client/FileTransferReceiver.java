package Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;

public class FileTransferReceiver extends Thread {
    private Socket socket;
    private String pathFolder = "ReceivedFiles/";
    private String pathFile;
    private UUID uuid;

    public FileTransferReceiver(Socket socket, String fileName, UUID uuid) {
        this.socket = socket;
        this.pathFile = pathFolder + fileName;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            socket.getOutputStream().write(uuid.toString().getBytes());
            socket.getOutputStream().flush();
            InputStream waitForConfirmation = socket.getInputStream();
            byte[] confirmation = new byte[5];
            waitForConfirmation.read(confirmation);
            System.out.println("DOWNLOAD IS STARTING");
            InputStream receiverInputStream = socket.getInputStream();
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
