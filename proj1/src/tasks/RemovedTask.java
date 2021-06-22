package tasks;

import files.Chunk;
import jobs.ReBackupChunk;
import messages.RemovedMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

/**
 * Task responsible to process a REMOVED Message
 */
public class RemovedTask extends Task {
    /**
     * @param message REMOVED message received on the multicast channel
     * @param peer    Peer responsible for this task
     */
    public RemovedTask(RemovedMessage message, Peer peer) {
        super(message, peer);
    }

    /**
     * This method checks if the REMOVED message is related to a stored chunk, if it is it will remove the sender ID
     * from the chunk's peer set, and if the new replication degree is lower than the desired replication degree
     * it will trigger a backup operation for this chunk. Otherwise, if this REMOVED message is related to a sent chunk
     * it will only remove the sender ID from the chunk's peer set
     */
    @Override
    public void run() {
        // System.out.println("[PEER] Received a REMOVED message");

        Chunk chunk;

        // checking if peer has that chunk stored
        if (this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            chunk.getPeers().remove(message.getSenderId());

            chunk.setReceivedPutchunk(false);

            if (chunk.getPeers().size() < chunk.getReplicationDegree() && !chunk.receivedPutchunk()) {
                int timeout = getSleepTimeDefault();
                this.peer.getRequestsExecutor().schedule(new ReBackupChunk(chunk, peer), timeout, TimeUnit.MILLISECONDS);
            }
        }
        // checking if this a backed up chunk sent
        else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            chunk.getPeers().remove(message.getSenderId());
        }
    }
}
