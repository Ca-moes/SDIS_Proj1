package tasks;

import files.SavedChunk;
import messages.DeleteMessage;
import peer.Peer;

public class DeleteTask extends Task {
    public DeleteTask(DeleteMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        // System.out.println("Received Delete Command for fileId: " + message.getFileId());
        for (String chunkId : this.peer.getInternalState().getSavedChunksMap().keySet()) {
            SavedChunk chunk = this.peer.getInternalState().getSavedChunksMap().get(chunkId);
            if (chunk.getFileId().equals(message.getFileId())) {
                this.peer.getInternalState().deleteChunk(chunk);
                this.peer.getInternalState().getSavedChunksMap().remove(chunk.getChunkId());
                this.peer.getInternalState().commit();
            }
        }
    }
}
