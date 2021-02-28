package files;

import java.io.Serializable;
import java.util.Arrays;

public class Chunk implements Serializable {
    private String fileId;
    private int chunkId;
    private int replicationDegree;
    private byte[] body;

    public Chunk(String fileId, int chunkId, int replicationDegree, byte[] body) {
        this.fileId = fileId;
        this.chunkId = chunkId;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkId=" + chunkId +
                ", replicationDegree=" + replicationDegree +
                ", body=" + Arrays.toString(body) +
                '}';
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public byte[] getBody() {
        return body;
    }
}
