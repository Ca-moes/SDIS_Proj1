package files;

import java.io.Serializable;
import java.util.Objects;

public class SavedChunk implements Serializable {
    private final String chunkId;
    private final int replicationDegree;
    private final int currentReplicationDegree;

    public SavedChunk(String chunkId, int replicationDegree, int currentReplicationDegree) {
        this.chunkId = chunkId;
        this.replicationDegree = replicationDegree;
        this.currentReplicationDegree = currentReplicationDegree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedChunk chunk = (SavedChunk) o;
        return Objects.equals(chunkId, chunk.chunkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkId);
    }

    public String getChunkId() {
        return chunkId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public int getCurrentReplicationDegree() {
        return currentReplicationDegree;
    }

    @Override
    public String toString() {
        return "SavedChunk{" +
                "chunkId='" + chunkId + '\'' +
                ", replicationDegree=" + replicationDegree +
                ", currentReplicationDegree=" + currentReplicationDegree +
                '}';
    }
}
