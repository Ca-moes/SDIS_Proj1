package tasks;

import messages.DeleteMessage;
import peer.Peer;

public class DeleteTask extends Task {
    public DeleteTask(DeleteMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // Check UML
        // TODO
    }
}
