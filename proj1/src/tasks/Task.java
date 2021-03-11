package tasks;

import messages.Message;
import peer.Peer;

import java.util.concurrent.ThreadLocalRandom;

public abstract class Task {
    protected final Message message;
    protected final Peer peer;

    public Task(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    abstract public void start();

    protected void sleep() {
        try {
            int sleep = ThreadLocalRandom.current().nextInt(0, 401);
            // System.out.printf("[%s] - sleeping for %3d ms%n", message.getType(), sleep);
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
