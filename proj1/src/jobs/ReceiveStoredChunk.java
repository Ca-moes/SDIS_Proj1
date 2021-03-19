package jobs;

import files.Chunk;
import peer.Peer;

public class ReceiveStoredChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final int timeout;

    public ReceiveStoredChunk(Chunk chunk, Peer peer, int timeout) {
        this.chunk = chunk;
        this.peer = peer;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
            this.peer.getIOExecutor().submit(new BackupChunk(chunk, peer, timeout*2));
        } else {
            this.peer.getInternalState().commit();
            System.out.println("[BACKUP] Chunk Backed Up - " + chunk.getChunkId());
        }
    }
}
