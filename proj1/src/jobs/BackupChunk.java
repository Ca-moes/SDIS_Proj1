package jobs;

import files.Chunk;
import messages.Message;
import messages.PutchunkMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

public class BackupChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final int timeout;

    public BackupChunk(Chunk chunk, Peer peer, int timeout) {
        this.chunk = chunk;
        this.peer = peer;
        this.timeout = timeout;
    }

    @Override
    public void run() {
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
