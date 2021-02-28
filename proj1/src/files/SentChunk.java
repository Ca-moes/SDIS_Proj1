package files;

import peer.Peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SentChunk extends Chunk implements Serializable {
    private List<String> peers;

    public SentChunk(String fileId, int chunkNo, int replicationDegree) {
        super(fileId, chunkNo, replicationDegree);
        this.peers = new ArrayList<>();
    }

    public SentChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
        this.peers = new ArrayList<>();
    }


    @Override
    public int getReplicationDegree() {
        return replicationDegree;
    }

    public List<String> getPeers() {
        return peers;
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
