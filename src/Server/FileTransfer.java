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
                FileTransferClient client = new FileTransferClient(socket);
                client.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void initiateFileTransfer(List<FileTransferClient> fileTransferClientList) {
        FileTransferClient sender = null;
        FileTransferClient receiver = null;
        for (FileTransferClient fileTransferClient: fileTransferClientList){
            if(fileTransferClient.getRole().equals("s")){
                sender = fileTransferClient;
            } else {
                receiver = fileTransferClient;
            }
        }
        sender.setOtherClient(receiver.getSocket());
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
