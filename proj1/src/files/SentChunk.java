package files;

import peer.Peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SentChunk implements Serializable {
    private final String chunkId;
    private final int replicationDegree;
    private final List<Peer> peers;

    public SentChunk(String chunkId, int replicationDegree, List<Peer> peers) {
        this.chunkId = chunkId;
        this.replicationDegree = replicationDegree;
        this.peers = peers;
    }

    public SentChunk(String chunkId, int replicationDegree) {
        this.chunkId = chunkId;
        this.replicationDegree = replicationDegree;
        this.peers = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SentChunk{" +
                "chunkId='" + chunkId + '\'' +
                ", replicationDegree=" + replicationDegree +
                ", peers=" + peers +
                '}';
    }

    public String getChunkId() {
        return chunkId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public List<Peer> getPeers() {
        return peers;
    }
}
