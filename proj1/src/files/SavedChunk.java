package files;

import java.io.Serializable;

public class SavedChunk extends Chunk implements Serializable {
    private boolean isBeingHandled = false;
    private boolean alreadyProvided = false;

    public SavedChunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(fileId, chunkNo, replicationDegree);
        this.setBody(body);
    }

    public SavedChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
        this.body = new byte[0];
    }

    public SavedChunk(SentChunk chunk) {
        super(chunk.getFileId(), chunk.getChunkNo());
        this.body = chunk.getBody();
    }

    @Override
    public String toString() {
        return String.format("[SavedChunk] FileID: %s | ChunkNo: %-4d | Desired Replication Degree: %d | Perceived Replication Degree: %d | Size: %.2fKB", fileId, chunkNo, replicationDegree, peers.size(), getSize());
    }

    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }

    public void setAlreadyProvided(boolean alreadyProvided) {
        this.alreadyProvided = alreadyProvided;
    }

    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    public boolean isAlreadyProvided() {
        return alreadyProvided;
    }
}
