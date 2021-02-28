package tasks;

import messages.ChunkMessage;
import peer.Peer;

public class ChunkTask extends Task {
    public ChunkTask(ChunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
