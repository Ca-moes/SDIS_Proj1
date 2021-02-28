package messages;

import peer.Peer;
import tasks.StoredTask;
import tasks.Task;

public class StoredMessage extends Message {
    public StoredMessage(String protocolVersion, String senderId, String fileId, int chunkNo, int replicationDegree) {
        super(protocolVersion, "STORED", senderId, fileId, chunkNo, replicationDegree, new byte[0]);
    }

    @Override
    public Task createTask(Peer peer) {
        return new StoredTask(this, peer);
    }
}
