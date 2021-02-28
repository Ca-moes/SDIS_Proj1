package tasks;

import messages.Message;
import peer.Peer;

public abstract class Task {
    protected final Message message;
    protected final Peer peer;

    public Task(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    abstract public void start();
}
