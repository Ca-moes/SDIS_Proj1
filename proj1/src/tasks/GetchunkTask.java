package tasks;

import files.SavedChunk;
import jobs.SendChunk;
import messages.GetchunkMessage;
import peer.Peer;

import java.util.concurrent.TimeUnit;

public class GetchunkTask extends Task {
    public GetchunkTask(GetchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void run() {
        if (!this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            // System.out.printf("[GETCHUNK] I dont have that chunk! %s\n", message.getFileId() + "_" + message.getChunkNo());
            return;
        }

        SavedChunk chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
        this.peer.getInternalState().fillBodyFromDisk(chunk);

        if (chunk.getBody() == null) {
            // System.out.println("[GETCHUNK] I was supposed to have that chunk, but I don't!");
            return;
        }

        // this peer is already handling this chunk
        if (chunk.isBeingHandled()) {
            return;
        }

        chunk.setBeingHandled(true);
        chunk.setAlreadyProvided(false);

        int timeout = getSleepTime();
        this.peer.getRequestsExecutor().schedule(new SendChunk(chunk, peer), timeout, TimeUnit.MILLISECONDS);
    }
}
