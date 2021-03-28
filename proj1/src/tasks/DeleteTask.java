package tasks;

import files.SavedChunk;
import messages.DeleteMessage;
import peer.Peer;

import java.util.Map;

/**
 * Task responsible to process a DELETE Message
 */
public class DeleteTask extends Task {
    /**
     * @param message DELETE message received on the multicast channel
     * @param peer Peer responsible for this task
     */
    public DeleteTask(DeleteMessage message, Peer peer) {
        super(message, peer);
    }

    /**
     * Method to delete every chunk whose file ID is the same as the one received on the message
     */
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
