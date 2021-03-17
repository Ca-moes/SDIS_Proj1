package tasks;

import files.Chunk;
import jobs.BackupChunk;
import jobs.ReBackupChunk;
import messages.Message;
import messages.PutchunkMessage;
import messages.RemovedMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

public class RemovedTask extends Task {
    public RemovedTask(RemovedMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void run() {
        // System.out.println("[PEER] Received a REMOVED message");

        Chunk chunk;

        // checking if peer has that chunk stored
        if (this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            chunk.getPeers().remove(message.getSenderId());

            chunk.setReceivedPutchunk(false);

            if (chunk.getPeers().size() < chunk.getReplicationDegree() && !chunk.receivedPutchunk()) {
                int timeout = getSleepTimeDefault();
                this.peer.getRequestsExecutor().schedule(new ReBackupChunk(chunk, peer), timeout, TimeUnit.MILLISECONDS);
            }
        }
        // checking if this a backed up chunk sent
        else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            chunk = this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            chunk.getPeers().remove(message.getSenderId());
        }
    }
}
