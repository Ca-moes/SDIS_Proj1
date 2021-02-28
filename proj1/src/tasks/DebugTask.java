package tasks;

import messages.DebugMessage;
import peer.Peer;

import java.util.Arrays;

public class DebugTask extends Task {
    public DebugTask(DebugMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        System.out.println("[DEBUG TASK] - started");
        System.out.println(new String(this.message.getBody()));
    }
}
