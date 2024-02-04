package Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            socket.getOutputStream().write(uuid.toString().getBytes());
            socket.getOutputStream().write("r".getBytes());
            socket.getOutputStream().flush();
            InputStream receiverInputStream = socket.getInputStream();
            byte[] confirmation = new byte[5];
            receiverInputStream.read(confirmation);
            byte[] checkSum = new byte[32];
            receiverInputStream.read(checkSum);
            InputStream receiverFile = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(pathFile);
            DigestInputStream digestInputStream = new DigestInputStream(receiverFile, digest);
            digestInputStream.transferTo(fileOutputStream);
            fileOutputStream.close();
            byte[] calculatedChecksum = digest.digest();
            boolean checksumMatch = MessageDigest.isEqual(checkSum, calculatedChecksum);
            if (checksumMatch) {
                System.out.println("Checksum verification successful. File received and saved at: " + pathFile);
            } else {
                System.out.println("Checksum verification failed. File may be corrupted.");
            }

            System.out.println("File received and saved at: " + pathFile);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
}
