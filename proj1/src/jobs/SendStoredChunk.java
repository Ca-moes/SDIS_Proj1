package jobs;

import files.Chunk;
import files.SavedChunk;
import messages.Message;
import peer.Peer;

/**
 * Job responsible to send STORED messages, this is part of the Backup sub protocol enhancement, and the
 * second part of the PUTCHUNK task
 *
 * @see tasks.PutchunkTask
 */
public class SendStoredChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final Message message;

    /**
     * @param chunk   Chunk related to this backup procedure
     * @param peer    Peer responsible for this job
     * @param message STORED message to be sent
     *
     * @see messages.StoredMessage
     * @see tasks.StoredTask
     */
    public SendStoredChunk(Chunk chunk, Peer peer, Message message) {
        this.chunk = chunk;
        this.peer = peer;
        this.message = message;
    }

    /**
     * <p>Method to start this job, it will perform the necessary checks, and if everything is OK it will send
     * the STORED message and store the chunk on its file system.</p>
     *
     * <strong>BACKUP ENHANCEMENT</strong>
     * <p>
     * This protocol is by default enhanced as it doesn't affect interoperability in any way or form,
     * the principle is very simple: when this Peer is waiting to start this job (between PUTCHUNK task and this job)
     * it will update this Chunk's STORED confirmations concurrently. This will be used when this job starts as it
     * will check if the number os STORED messages (i.e. Actual Replication Degree) is lower than the desired
     * replication degree, if this checks out it will store the chunk and send the STORED message, otherwise it will
     * remove the chunk from the stored map and nothing else.
     * </p>
     * <p>
     * Also, this enhancement is composed of two very useful changes: the sleep time lower boundary is calculated
     * considering the occupation rate of this peer. As documented on the Task class</p>
     *
     * @see tasks.Task
     */
    @Override
    public void run() {
        // System.out.println("[DEBUG] SEND STORED CHUNK STARTED!");
        if (chunk.isStored() && peer.getInternalState().getSavedChunksMap().containsKey(chunk.getChunkId())) {
            // peer has this chunk stored and it will send a stored anyways
            peer.getMulticastControl().sendMessage(message);
        } else if (!peer.getInternalState().getSentChunksMap().containsKey(chunk.getChunkId())) {
            if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
                if (chunk.getBody().length + this.peer.getInternalState().getOccupation() < this.peer.getInternalState().getCapacity()) {
                    // This peer will save the chunk locally
                    peer.getMulticastControl().sendMessage(message);
                    peer.getInternalState().storeChunk((SavedChunk) chunk);
                    chunk.setStored(true);
                    chunk.getPeers().add(peer.getPeerId());
                    peer.getInternalState().commit();
                    System.out.printf("[BACKUP] Saved chunk %s\n", chunk.getChunkId());
                } else {
                    System.out.printf("[PIS] Not enough space for %s\n", chunk.getChunkId());
                }
            } else {
                // no need to backup here as it is already being backed up and it wont reply with STORED
                peer.getInternalState().getSavedChunksMap().remove(chunk.getChunkId());
            }
        }
        chunk.setReceivedPutchunk(false);
    }
}
