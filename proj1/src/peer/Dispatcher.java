package peer;

import messages.Message;

public class Dispatcher implements Runnable {
    private final byte[] packet;
    private final Peer peer;
    private final int packetLength;

    public Dispatcher(byte[] packet, Peer peer, int packetLength) {
        this.packet = packet;
        this.peer = peer;
        this.packetLength = packetLength;
    }

    @Override
    public void run() {
        try {
            Message m = Message.fromDatagramPacket(packet, packetLength);
            // if isOwner we discard the message
            if (!m.isOwner(this.peer.getPeerId())) {
                m.createTask(peer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
