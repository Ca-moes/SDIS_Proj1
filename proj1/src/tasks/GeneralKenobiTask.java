package tasks;

import messages.DeleteMessage;
import messages.GeneralKenobi;
import messages.Message;
import peer.Peer;

/**
 * Task responsible to process a GENERALKENOBI Message
 */
public class GeneralKenobiTask extends Task {
    /**
     * @param message GENERALKENOBI message received on the multicast channel
     * @param peer    Peer responsible for this task
     */
    public GeneralKenobiTask(GeneralKenobi message, Peer peer) {
        super(message, peer);
    }

    /**
     * <strong>Delete Enhancement</strong>
     * <p>
     * The concept is simple, when a peer comes online, if it is enhanced it will send a GENERALKENOBI
     * message saying "Hello There" to other enhanced peers listening at the moment, the purpose of this
     * message is to received after that a DELETE message if another peer has deleted a file.
     * <p>
     * This peer may have been offline when a deletion occurred, so this is a workaround to that, it may not be
     * the optimal solution but is efficient and reliable
     * </p>
     *
     * <p>
     * Disclaimer: if you are not familiar with General Kenobi or Obi-Wan Kenobi, he is an iconic character in
     * the Star Wars franchise, often known to enter the scene with an "Hello There", so we thought it would
     * fit perfectly here. We had some fun developing this program, this enhancement and the State in HTML
     * are proof to that.
     * </p>
     */
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
