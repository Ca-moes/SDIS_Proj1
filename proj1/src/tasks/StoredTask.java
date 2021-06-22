package tasks;

import files.SavedChunk;
import files.SentChunk;
import messages.StoredMessage;
import peer.Peer;

/**
 * Task responsible to process a STORED Message
 */
public class StoredTask extends Task {
    /**
     * @param message STORED message received on the multicast channel
     * @param peer    Peer responsible for this task
     */
    public StoredTask(StoredMessage message, Peer peer) {
        super(message, peer);
    }

    /**
     * This method will update the confirmations on the chunk's, either if this is related to a sent chunk or a
     * saved chunk
     */
    @Override
    public void run() {
        SentChunk sentChunk = new SentChunk(message.getFileId(), message.getChunkNo());
        SavedChunk savedChunk = new SavedChunk(message.getFileId(), message.getChunkNo());

        peer.getInternalState().updateStoredConfirmation(sentChunk, message.getSenderId());
        peer.getInternalState().updateStoredConfirmation(savedChunk, message.getSenderId());
    }
}
