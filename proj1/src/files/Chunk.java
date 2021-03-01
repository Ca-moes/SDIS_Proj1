package files;

import java.io.Serializable;
import java.util.*;

public abstract class Chunk implements Serializable {
    protected String fileId;
    protected int chunkNo;
    protected int replicationDegree;
    protected final HashSet<String> peers;

    public Chunk(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = 0;
        this.peers = new HashSet<>();
    }

    public Chunk(String fileId, int chunkNo, int replicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.peers = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return chunkNo == chunk.chunkNo && Objects.equals(fileId, chunk.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, chunkNo);
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public String getFileId() {
        return fileId;
    }

    public String getChunkId() {
        return fileId + "_" + chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public HashSet<String> getPeers() {
        return peers;
    }
}
