package tasks;

import messages.GetchunkMessage;
import peer.Peer;

public class GetchunkTask extends Task {
    public GetchunkTask(GetchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
