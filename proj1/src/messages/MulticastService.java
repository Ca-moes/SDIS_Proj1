package messages;

import peer.Dispatcher;
import peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

/**
 * Class responsible to Send and Receive messages through multicast
 */
public class MulticastService extends MulticastSocket implements Runnable {
    private final InetAddress address;
    private final Peer peer;
    private final String identifier;

    /**
     * Constructor for the multicast service
     *
     * @param address    Address for the multicast socket
     * @param port       Port for the multicast socket
     * @param peer       Peer controlling the socket
     * @param identifier Service ID (MC | MDB | MDR)
     * @throws IOException On error creating the socket
     */
    public MulticastService(InetAddress address, int port, Peer peer, String identifier) throws IOException {
        super(port);
        this.address = address;
        this.peer = peer;
        this.identifier = identifier;

        this.setTimeToLive(1);
        this.joinGroup(this.address);
        System.out.printf("[MULTICAST SERVICE] [%s] Service is now Online\n", this.identifier);
    }

    /**
     * Method to send a message through multicast
     *
     * @param message Message to be sent
     * @return true if the sending operation is successful
     */
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

    /**
     * Method to be receiving packets and sending them to the dispatcher for triage right as they arrive
     */
    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[65507];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.receive(packet);
                // send the message to triage so it can be sent to an appropriate worker then
                peer.getTriageExecutor().submit(new Dispatcher(Arrays.copyOf(buffer, buffer.length), peer, packet.getLength()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
