package tasks;

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

        SentChunk chunk = new SentChunk(message.getFileId(), message.getChunkNo());

        peer.getInternalState().updateBackedUpChunks(chunk, message.getSenderId());
    }
}
