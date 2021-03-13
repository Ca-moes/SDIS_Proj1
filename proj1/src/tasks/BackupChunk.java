package tasks;

import files.SentChunk;
import messages.Message;
import peer.Peer;

public class BackupChunk implements Runnable {
    private final Message message;
    private final Peer peer;

    public BackupChunk(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        SentChunk chunk = new SentChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree());

        this.peer.getInternalState().getSentChunksMap().put(chunk.getChunkId(), chunk);

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
