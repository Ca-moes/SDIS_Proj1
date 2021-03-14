package tasks;

import files.Chunk;
import messages.RemovedMessage;
import peer.Peer;

public class RemovedTask extends Task {
    public RemovedTask(RemovedMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        System.out.println("[PEER] Received a REMOVED message");

        Chunk chunk = null;

        // checking if peer has that chunk stored
        if (this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
        }
        // checking if this a backed up chunk sent
        else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
        }

        if (chunk != null) {
            chunk.getPeers().remove(message.getSenderId());
            if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
                System.out.println("[PEER] Need to start a new backup operation for this chunk");
            }
        }
    }
}
