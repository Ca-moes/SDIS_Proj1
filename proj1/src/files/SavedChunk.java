package files;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SavedChunk extends Chunk implements Serializable {
    private final transient byte[] body;

    public SavedChunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(fileId, chunkNo, replicationDegree);
        this.body = body;
    }

    public SavedChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
        this.body = new byte[0];
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "\nSavedChunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                ", peers=" + peers +
                '}';
    }
}
