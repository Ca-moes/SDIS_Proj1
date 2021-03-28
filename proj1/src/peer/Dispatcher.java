package peer;

import messages.Message;

import java.util.concurrent.ExecutorService;

/**
 * Dispatcher to process messages and start tasks (works like triage)
 */
public class Dispatcher implements Runnable {
    private final byte[] packet;
    private final Peer peer;
    private final int packetLength;

    /**
     * Constructor for this Dispatcher
     *
     * @param packet       Packet received on the multicast channel
     * @param peer         Peer responsible for the triage
     * @param packetLength Size of the packet
     */
    public Dispatcher(byte[] packet, Peer peer, int packetLength) {
        this.packet = packet;
        this.peer = peer;
        this.packetLength = packetLength;
    }

    /**
     * Method to perform the triage of the received packet, it will create an appropriate
     * message and start an also appropriate task
     */
    @Override
    public void run() {
        try {
            Message m = Message.fromDatagramPacket(packet, packetLength);
            // if isOwner we discard the message
            if (!m.isOwner(this.peer.getPeerId())) {
                // get the correspondent worker to do the job
                ExecutorService worker = m.getWorker(this.peer);
                worker.submit(m.createTask(peer));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
