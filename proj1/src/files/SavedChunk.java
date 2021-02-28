package files;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SavedChunk extends Chunk implements Serializable {
    private int currentReplicationDegree;
    private byte[] body;

    public SavedChunk(String fileId, int chunkNo, int replicationDegree, int currentReplicationDegree, byte[] body) {
        super(fileId, chunkNo, replicationDegree);
        this.currentReplicationDegree = currentReplicationDegree;
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public int getCurrentReplicationDegree() {
        return currentReplicationDegree;
    }

    @Override
    public String toString() {
        return "\nSavedChunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                ", currentReplicationDegree=" + currentReplicationDegree +
                /*", body=" + Arrays.toString(body) +*/
                '}';
    }
}
