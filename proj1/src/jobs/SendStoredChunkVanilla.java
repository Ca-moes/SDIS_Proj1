package jobs;

import files.Chunk;
import files.SavedChunk;
import messages.Message;
import peer.Peer;

/**
 * Job responsible to send STORED messages, and the
 * second part of the PUTCHUNK task
 *
 * @see tasks.PutchunkTask
 */
public class SendStoredChunkVanilla implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final Message message;

    /**
     * @param chunk   Chunk related to this backup procedure
     * @param peer    Peer responsible for this job
     * @param message STORED message to be sent
     * @see messages.StoredMessage
     * @see tasks.StoredTask
     */
    public SendStoredChunkVanilla(Chunk chunk, Peer peer, Message message) {
        this.chunk = chunk;
        this.peer = peer;
        this.message = message;
    }

    /**
     * Method to start this job, it will perform the necessary checks, and if everything is OK it will send
     * the STORED message and store the chunk on its file system.
     */
    @Override
    public void run() {
        // System.out.println("[DEBUG] SEND STORED CHUNK STARTED!");
        if (chunk.isStored() && peer.getInternalState().getSavedChunksMap().containsKey(chunk.getChunkId())) {
            // peer has this chunk stored and it will send a stored anyways
            peer.getMulticastControl().sendMessage(message);
        } else if (!peer.getInternalState().getSentChunksMap().containsKey(chunk.getChunkId())) {
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
        }
        chunk.setReceivedPutchunk(false);
    }
}
