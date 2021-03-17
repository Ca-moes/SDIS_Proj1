package tasks;

import messages.Message;
import peer.Peer;

import java.util.concurrent.ThreadLocalRandom;

public abstract class Task implements Runnable {
    protected final Message message;
    protected final Peer peer;

    public Task(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    protected int getSleepTimeDefault() {
        return ThreadLocalRandom.current().nextInt(0, 401);
    }

    /**
     * Enhancement 1 - Sleeping Lower Bound is determinate by the occupation rate, meaning peers with higher occupation
     * rate will sleep (probably) for longer than the other peers
     * @return sleep time (milliseconds)
     */
    protected int getSleepTime() {
        int lowerBound = (int) Math.sin((double) peer.getInternalState().calculateOccupation() / peer.getInternalState().getCapacity() * 1.5) * 400;
        return ThreadLocalRandom.current().nextInt(lowerBound, 401);
    }
}
