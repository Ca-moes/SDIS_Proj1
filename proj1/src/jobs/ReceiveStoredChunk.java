package jobs;

import files.Chunk;
import peer.Peer;

/**
 * Job responsible for checking the stored messages received for a chunk,
 * this job is the second part of the BackupChunk job
 *
 * @see BackupChunk
 * @see messages.StoredMessage
 * @see tasks.StoredTask
 */
public class ReceiveStoredChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final int timeout;

    /**
     * @param chunk   Chunk to check the actual replication degree
     * @param peer    Peer responsible for this job
     * @param timeout Timeout to pass to the BackupChunk job if needed
     */
    public ReceiveStoredChunk(Chunk chunk, Peer peer, int timeout) {
        this.chunk = chunk;
        this.peer = peer;
        this.timeout = timeout;
    }

    /**
     * Method to start the job, it will check if the Chunk's actual replication degree is lower than
     * the desired, if it is it will start another BackupChunk right away with twice the timeout, otherwise it will
     * just commit the database and finish as no other action is required
     */
    @Override
    public void run() {
        if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
            this.peer.getIOExecutor().submit(new BackupChunk(chunk, peer, timeout * 2));
        } else {
            this.peer.getInternalState().commit();
            System.out.println("[BACKUP] Chunk Backed Up - " + chunk.getChunkId());
        }
    }
}
