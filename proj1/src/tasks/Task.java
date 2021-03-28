package tasks;

import messages.Message;
import peer.Peer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Task abstract super class
 */
public abstract class Task implements Runnable {
    protected final Message message;
    protected final Peer peer;

    /**
     * Super Constructor for a Task
     *
     * @param message Message received on the multicast channel
     * @param peer    Peer responsible for this task
     */
    public Task(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    /**
     * Default method to get the sleep time from 0-400ms
     *
     * @return The sleep time, random from 0 to 400ms
     */
    protected int getSleepTimeDefault() {
        return ThreadLocalRandom.current().nextInt(0, 401);
    }

    /**
     * Sleeping Lower Bound is determinate by the occupation rate, meaning peers with higher occupation
     * rate will sleep (probably) for longer than the other peers
     *
     * @return The sleep time (milliseconds)
     */
    protected int getSleepTime() {
        int lowerBound = (int) Math.sin((double) peer.getInternalState().calculateOccupation() / peer.getInternalState().getCapacity() * 1.5) * 400;
        return ThreadLocalRandom.current().nextInt(lowerBound, 401);
    }
}
