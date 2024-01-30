package Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FIleTransfer extends Thread {
    private ArrayList<Socket> clients = new ArrayList<>();
    private ServerSocket transferSocket;

    public FIleTransfer(ServerSocket transferSocket){
        this.transferSocket = transferSocket;
    }
    @Override
    public void run() {
        waitForClientsToJoin();

    }

    private void waitForClientsToJoin(){
        while (clients.size() < 2) {
            try {
                Socket socket = transferSocket.accept();
                clients.add(socket);
                System.out.println("ADDED");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("USERS HAVE JOINED");
        initiateFileTransfer();
    }

    private void initiateFileTransfer() {
        Socket senderSocket = clients.get(0);
        Socket receiverSocket = clients.get(1);

        try {
            while (true) {
                InputStream senderInputStream = senderSocket.getInputStream();
                OutputStream receiverOutputStream = receiverSocket.getOutputStream();
                senderInputStream.transferTo(receiverOutputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
