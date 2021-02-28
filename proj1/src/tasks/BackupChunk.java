package tasks;

import files.SentChunk;
import messages.Message;
import peer.Peer;

import java.util.ArrayList;
import java.util.List;

public class BackupChunk implements Runnable {
    private final Message message;
    private final Peer peer;

    public BackupChunk(Message message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        List<String> peers = new ArrayList<>();

        SentChunk chunk = new SentChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree());

        this.peer.getInternalState().getSentChunksMap().put(chunk, peers);

        int timeout = 1000;
        while (timeout <= 16000) {
            try {
                this.peer.getMulticastDataBackup().sendMessage(message);
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (peers.size() < message.getReplicationDegree()) {
                timeout = 2 * timeout;
            } else {
                this.peer.getInternalState().commit();
                break;
            }
        }
    }
}
