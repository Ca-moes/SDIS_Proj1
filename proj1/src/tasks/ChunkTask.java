package tasks;

import messages.ChunkMessage;
import peer.Peer;

public class ChunkTask extends Task {
    public ChunkTask(ChunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        if (this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            // this chunk is stored here, so it will be marked as "already provided by another peer"
            this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo()).setAlreadyProvided(true);
        } else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            // this chunk is being retrieved for restoration
            this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo()).setBody(message.getBody());
        }
    }
}
