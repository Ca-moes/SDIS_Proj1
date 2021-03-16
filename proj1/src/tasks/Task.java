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

    protected void sleep() {
        try {
            double lowerBound = Math.sin((double) peer.getInternalState().calculateOccupation() / peer.getInternalState().getCapacity() * 1.5) * 400;
            //System.out.printf("OCU: %d\nCAP: %d\n", peer.getInternalState().getPeerOccupation(), peer.getInternalState().getCapacity());
            enhancedSleep((int) lowerBound);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void enhancedSleep(int lowerBound) throws InterruptedException {
        int sleep = ThreadLocalRandom.current().nextInt(lowerBound, 401);
        //System.out.printf("[%s] - (lower-%d) sleeping for %3d ms%n", message.getType(), lowerBound, sleep);
        Thread.sleep(sleep);
    }
}
