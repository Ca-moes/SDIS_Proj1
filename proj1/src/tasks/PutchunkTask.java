package tasks;

import files.SavedChunk;
import jobs.SendStoredChunk;
import jobs.SendStoredChunkVanilla;
import messages.Message;
import messages.PutchunkMessage;
import messages.StoredMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

/**
 * Task responsible to process a PUTCHUNK Message
 */
public class PutchunkTask extends Task {
    /**
     * @param message PUTCHUNK message received on the multicast channel
     * @param peer    Peer responsible for this task
     */
    public PutchunkTask(PutchunkMessage message, Peer peer) {
        super(message, peer);
    }

    /**
     * This method will perform the necessary checks to store a chunk (space needed, if it already is saving the chunk,
     * etc.) then it will start the SendStoredChunk job after a delay, this job is enhanced by default as it does not
     * interfere with the peers interoperability, the enhancement details can be found on the SendStoredChunk class
     *
     * @see SendStoredChunk
     */
    @Override
    public void run() {
        SavedChunk chunk = new SavedChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree(), message.getBody());

        Message reply = new StoredMessage(peer.getProtocolVersion(), peer.getPeerId(), message.getFileId(), message.getChunkNo());

        if (!this.peer.getInternalState().isAcceptingRequests()) {
            // System.out.println("[PUTCHUNK] Peer is not accepting requests as of now. It is probably reclaiming some space.");
            return;
        }

        if (chunk.isStored() && this.peer.getInternalState().getSavedChunksMap().containsKey(chunk.getChunkId())) {
            // This peer has this chunk but it will send a reply anyways cause it indicates that it has saved the chunk (UDP unreliability)
            chunk.setReceivedPutchunk(true);
            peer.getRequestsExecutor().schedule(new SendStoredChunk(chunk, peer, reply), this.getSleepTime(), TimeUnit.MILLISECONDS);
        } else if (!this.peer.getInternalState().getSentChunksMap().containsKey(chunk.getChunkId())) {
            // This peer has no storage left to store the chunk received
            if (chunk.getBody().length + this.peer.getInternalState().getOccupation() > this.peer.getInternalState().getCapacity()) {
                // this peer will try to free space by removing chunks which have higher replication degree than desired
                if (this.peer.getInternalState().freeSpace()) {
                    // it will now check again if there's enough space to store the chunk
                    if (chunk.getBody().length + this.peer.getInternalState().getOccupation() > this.peer.getInternalState().getCapacity()) {
                        // I dont have the storage needed to backup that, i'm afraid
                        System.out.printf("[PIS] Not enough space for %s\n", chunk.getChunkId());
                    } else {
                        // it has enough space and it will store the message
                        peer.getInternalState().getSavedChunksMap().put(chunk.getChunkId(), chunk);
                        peer.getRequestsExecutor().schedule(new SendStoredChunk(chunk, peer, reply), this.getSleepTime(), TimeUnit.MILLISECONDS);
                    }
                } else {
                    // couldn't try to free space, maybe some other putchunk process is trying to
                    System.out.printf("[PIS] Not enough space for %s\n", chunk.getChunkId());
                }
            } else {
                // there's enough space, wont even try to free some
                peer.getInternalState().getSavedChunksMap().put(chunk.getChunkId(), chunk);

                if (this.peer.isEnhanced()) {
                    peer.getRequestsExecutor().schedule(new SendStoredChunk(chunk, peer, reply), this.getSleepTime(), TimeUnit.MILLISECONDS);
                } else {
                    peer.getRequestsExecutor().schedule(new SendStoredChunkVanilla(chunk, peer, reply), this.getSleepTime(), TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
