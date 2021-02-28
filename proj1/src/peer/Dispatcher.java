package peer;

import messages.Message;

public class Dispatcher implements Runnable {
    private final Message message;
    private final Peer peer;

    public Dispatcher(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        this.message.createTask(this.peer).start();
    }
}
