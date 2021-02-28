package messages;

import peer.Peer;
import tasks.DeleteTask;
import tasks.Task;

public class DeleteMessage extends Message {
    public DeleteMessage(String protocolVersion, String senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(protocolVersion, "DELETE", senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    public Task createTask(Peer peer) {
        return new DeleteTask(this, peer);
    }
}
