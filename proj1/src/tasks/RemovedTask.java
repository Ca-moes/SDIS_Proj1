package tasks;

import messages.Message;
import messages.RemovedMessage;
import peer.Peer;

public class RemovedTask extends Task {
    public RemovedTask(RemovedMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
