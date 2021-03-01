package tasks;

import files.SavedChunk;
import files.SentChunk;
import messages.StoredMessage;
import peer.Peer;

public class StoredTask extends Task {
    public StoredTask(StoredMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        System.out.println("Received STORED from " + message.getSenderId());

        SentChunk sentChunk = new SentChunk(message.getFileId(), message.getChunkNo());
        SavedChunk savedChunk = new SavedChunk(message.getFileId(), message.getChunkNo());

        peer.getInternalState().updateStoredConfirmation(sentChunk, message.getSenderId());
        peer.getInternalState().updateStoredConfirmation(savedChunk, message.getSenderId());
    }
}
