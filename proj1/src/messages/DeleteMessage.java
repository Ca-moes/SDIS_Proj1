package messages;

import peer.Peer;
import tasks.DeleteTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;

public class DeleteMessage extends Message {
    public DeleteMessage(String protocolVersion, int senderId, String fileId) {
        super(protocolVersion, "DELETE", senderId, fileId, 0, 0, new byte[0]);
    }

    @Override
    public byte[] encodeToSend() {
        return String.format("%s %s %s %s \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Task createTask(Peer peer) {
        return new DeleteTask(this, peer);
    }
}
