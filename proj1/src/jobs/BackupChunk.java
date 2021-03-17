package jobs;

import files.Chunk;
import files.SentChunk;
import messages.Message;
import peer.Peer;

public class BackupChunk implements Runnable {
    private final Message message;
    private final Peer peer;
    private final boolean local;

    public BackupChunk(Message message, Peer peer, boolean local) {
        this.message = message;
        this.peer = peer;
        this.local = local;
    }

    @Override
    public void run() {
        Chunk chunk;
        if (!local) {
            chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
        } else {
            chunk = new SentChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree());
            this.peer.getInternalState().getSentChunksMap().put(chunk.getChunkId(), (SentChunk) chunk);
        }

        int timeout = 1000;
        while (timeout <= 32000) {
            try {
                this.peer.getMulticastDataBackup().sendMessage(message);
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (chunk.getPeers().size() < message.getReplicationDegree()) {
                timeout = 2 * timeout;
            } else {
                this.peer.getInternalState().commit();
                System.out.println("[PEER] Chunk Backed Up - " + chunk.getChunkId());
                break;
            }
        }
    }
}
