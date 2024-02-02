package Server;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class FileTransfer extends Thread {
    private static FileTransfer instance;
    private ServerSocket transferSocket;
    private Map<UUID, List<FileTransferClient>> clients = new HashMap<>();

    private FileTransfer() {
        try {
            transferSocket = new ServerSocket(1338);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized FileTransfer getInstance() {
        if (instance == null) {
            instance = new FileTransfer();
        }
        return instance;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket socket = transferSocket.accept();
                System.out.println("TRANSFER CLIENT +1");
                FileTransferClient client = new FileTransferClient(socket);
                client.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void initiateFileTransfer(List<FileTransferClient> fileTransferClientList) {
        FileTransferClient client1 = fileTransferClientList.get(0);
        FileTransferClient client2 = fileTransferClientList.get(1);
        client1.setOtherClient(client2.getSocket());
        client2.setOtherClient(client1.getSocket());
    }

    public void addClient(UUID uuid, FileTransferClient fileTransferClient){
        List<FileTransferClient> list = clients.get(uuid);
        if(list == null){
            list = new ArrayList<>();
        }
        list.add(fileTransferClient);
        clients.put(uuid, list);
        if(list.size() == 2){
            initiateFileTransfer(list);
            notifyClientsOfDownload(list);
        }
    }

    private void notifyClientsOfDownload(List<FileTransferClient> list) {
        list.forEach(fileTransferClient -> fileTransferClient.notifyClient());
    }
}
