package Server;

public class PingPongInteraction implements Runnable {
    private boolean disconnected = true;
    private final ServerSideClient client;
    private boolean responded = true;

    public PingPongInteraction(ServerSideClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while(disconnected) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            responded = false;
            client.sendToClient("PING");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(!responded) {
//                System.out.println("Did not receive an answer. Closing connection.");
                client.closeSocket();
                return;
            }
//            System.out.println("Receive an answer. Repeating.");
        }
    }

    public void disconnect() {
        disconnected = true;
    }

    public synchronized void receivedPong() {
        responded = true;
    }
}
