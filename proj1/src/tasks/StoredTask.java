package tasks;

import messages.Message;
import messages.StoredMessage;
import peer.Peer;

public class StoredTask extends Task {
    public StoredTask(StoredMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
