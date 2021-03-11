package messages;

import peer.Peer;
import tasks.GetchunkTask;
import tasks.Task;

public class GetchunkMessage extends Message {
    public GetchunkMessage(String protocolVersion, int senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(protocolVersion, "GETCHUNK", senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    public Task createTask(Peer peer) {
        return new GetchunkTask(this, peer);
    }
}
