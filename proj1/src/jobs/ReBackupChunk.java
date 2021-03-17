package jobs;

import files.Chunk;
import peer.Peer;

public class ReBackupChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;

    public ReBackupChunk(Chunk chunk, Peer peer) {
        this.chunk = chunk;
        this.peer = peer;
    }

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
