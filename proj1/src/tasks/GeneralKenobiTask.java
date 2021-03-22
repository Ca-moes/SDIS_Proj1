package tasks;

import messages.DeleteMessage;
import messages.GeneralKenobi;
import messages.Message;
import peer.Peer;

public class GeneralKenobiTask extends Task {
    public GeneralKenobiTask(GeneralKenobi message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void run() {
        if (this.peer.isEnhanced()) {
            System.out.printf("[GENERALKENOBI] Peer %d says Hello There\n", message.getSenderId());
            for (String deleted : this.peer.getInternalState().getDeletedFiles()) {
                System.out.printf("[GENERALKENOBI] Sending %s for deletion\n", deleted);
                Message message = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), deleted);
                this.peer.getMulticastControl().sendMessage(message);
            }
        }
    }
}
