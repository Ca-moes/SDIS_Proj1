package tasks;

import files.Chunk;
import messages.Message;
import messages.PutchunkMessage;
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
            chunk.getPeers().remove(message.getSenderId());

            chunk.setReceivedPutchunk(false);

            if (chunk.getPeers().size() < chunk.getReplicationDegree() && !chunk.receivedPutchunk()) {
                sleep();
                if (!chunk.receivedPutchunk()) {
                    this.peer.getInternalState().fillBodyFromDisk(chunk);
                    if (chunk.getBody() != null) {
                        Message message = new PutchunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(), chunk.getReplicationDegree(), chunk.getBody());
                        this.peer.getTaskExecutor().submit(new BackupChunk(message, peer, false));
                    }
                } else {
                    System.out.printf("[PEER] Already Received a PUTCHUNK for %s\n", chunk.getChunkId());
                }
            }
        }
        // checking if this a backed up chunk sent
        else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            chunk.getPeers().remove(message.getSenderId());
        }
    }
}
