package files;

import peer.Peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SentChunk extends Chunk implements Serializable {
    private transient byte[] body = null;

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

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void clearBody() {
        body = null;
    }

    public byte[] getBody() {
        return body;
    }
}
