package jobs;

import files.Chunk;
import peer.Peer;
import peer.PeerInternalState;

/**
 * Job to start a backup for a Chunk backed up by this peer, this job will be triggered for the RECLAIM sub protocol
 *
 * @see PeerInternalState#forceFreeSpace()
 * @see tasks.RemovedTask
 */
public class ReBackupChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;

    /**
     * @param chunk Chunk to be re-backed up
     * @param peer  Peer responsible for this Job
     */
    public ReBackupChunk(Chunk chunk, Peer peer) {
        this.chunk = chunk;
        this.peer = peer;
    }

    /**
     * Method to start the Backup Procedure for this Chunk
     */
    @Override
    public void run() {
        if (!chunk.receivedPutchunk()) {
            this.peer.getInternalState().fillBodyFromDisk(chunk);
            if (chunk.getBody() != null) {
                this.peer.getIOExecutor().submit(new BackupChunk(chunk, this.peer, 1));
            }
        } else {
            System.out.printf("[PEER] Already Received a PUTCHUNK for %s\n", chunk.getChunkId());
        }
    }
}
