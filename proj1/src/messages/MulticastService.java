package messages;

import peer.Dispatcher;
import peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastService extends MulticastSocket implements Runnable {
    private final InetAddress address;
    private final Peer peer;
    private final String identifier;

    public MulticastService(InetAddress address, int port, Peer peer, String identifier) throws IOException {
        super(port);
        this.address = address;
        this.peer = peer;
        this.identifier = identifier;

        this.setTimeToLive(1);
        this.joinGroup(this.address);
    }

    public boolean sendMessage(Message message) {
        byte[] buffer = message.encodeToSend();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.address, this.getLocalPort());
        try {
            this.send(packet);
            // System.out.printf("[MulticastService] (%s) - Sent %s Message - bytes sent: %d%n", this.identifier, message.getType(), buffer.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[65507];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.receive(packet);
                Message m = Message.fromDatagramPacket(packet);
                // if isOwner we discard the message
                if (!m.isOwner(this.peer.getPeerId())) {
                    // System.out.printf("[MulticastService] (%s) - Received %s Message from %s - bytes received in body: %d%n", this.identifier, m.getType(), m.getSenderId(), m.getBody().length);
                    peer.getThreadPoolExecutor().submit(new Dispatcher(m, peer));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
