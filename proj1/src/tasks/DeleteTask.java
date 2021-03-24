package tasks;

import files.SavedChunk;
import messages.DeleteMessage;
import peer.Peer;

import java.util.Map;

public class DeleteTask extends Task {
    public DeleteTask(DeleteMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void run() {
        System.out.println("[DELETE] FileID: " + message.getFileId());
        for (Map.Entry<String, SavedChunk> entry : this.peer.getInternalState().getSavedChunksMap().entrySet()) {
            SavedChunk chunk = entry.getValue();
            if (chunk.getFileId().equals(message.getFileId())) {
                this.peer.getInternalState().deleteChunk(chunk);
                this.peer.getInternalState().getSavedChunksMap().remove(chunk.getChunkId());
                this.peer.getInternalState().commit();
            }
        }
    }
}
