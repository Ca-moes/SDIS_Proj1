package files;

import java.io.Serializable;

public class SentChunk extends Chunk implements Serializable {
    public SentChunk(String fileId, int chunkNo, int replicationDegree) {
        super(fileId, chunkNo, replicationDegree);
    }

    public SentChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
    }

    @Override
    public int getReplicationDegree() {
        return replicationDegree;
    }

    @Override
    public String toString() {
        return "\nSentChunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                ", peers=" + peers +
                '}';
    }
}
