package tasks;

import messages.Message;
import messages.PutchunkMessage;
import peer.Peer;

public class PutchunkTask extends Task {
    public PutchunkTask(PutchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
