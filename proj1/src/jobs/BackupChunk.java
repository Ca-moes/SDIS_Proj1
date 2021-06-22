package jobs;

import files.Chunk;
import messages.Message;
import messages.PutchunkMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

/**
 * Job responsible to start the backup procedure for a Chunk given a timeout
 */
public class BackupChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final int timeout;

    /**
     * Constructor for this Job given the chunk, peer and timeout
     *
     * @param chunk   Chunk to be backed up
     * @param peer    Peer responsible for the backup procedure
     * @param timeout Timeout after witch it will trigger the Second part of this job
     * @see ReceiveStoredChunk
     */
    public BackupChunk(Chunk chunk, Peer peer, int timeout) {
        this.chunk = chunk;
        this.peer = peer;
        this.timeout = timeout;
    }

    /**
     * Method to send the PUTCHUNK message and start the second part of this job after the timeout
     *
     * @see ReceiveStoredChunk
     * @see PutchunkMessage
     */
    @Override
    public void run() {
        if (this.timeout >= 32) {
            System.out.println("[BACKUP] Chunk Could not be Backed Up - " + chunk.getChunkId());
            return;
        }

        Message message = new PutchunkMessage(
                this.peer.getProtocolVersion(),
                this.peer.getPeerId(),
                chunk.getFileId(),
                chunk.getChunkNo(),
                chunk.getReplicationDegree(),
                chunk.getBody());
        this.peer.getMulticastDataBackup().sendMessage(message);
        this.peer.getRequestsExecutor().schedule(new ReceiveStoredChunk(chunk, peer, timeout), timeout, TimeUnit.SECONDS);
    }
}
