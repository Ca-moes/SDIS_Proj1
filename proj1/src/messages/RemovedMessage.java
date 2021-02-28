package messages;

import peer.Peer;
import tasks.RemovedTask;
import tasks.Task;

public class RemovedMessage extends Message {
    public RemovedMessage(String protocolVersion, String senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(protocolVersion, "REMOVED", senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    public Task createTask(Peer peer) {
        return new RemovedTask(this, peer);
    }
}
