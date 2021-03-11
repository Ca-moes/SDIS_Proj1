package messages;

import peer.Peer;
import tasks.RemovedTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;

public class RemovedMessage extends Message {
    public RemovedMessage(String protocolVersion, int senderId, String fileId, int chunkNo) {
        super(protocolVersion, "REMOVED", senderId, fileId, chunkNo, 0, new byte[0]);
    }

    @Override
    public byte[] encodeToSend() {
        return String.format("%s %s %s %s %d \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId,
                this.chunkNo).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Task createTask(Peer peer) {
        return new RemovedTask(this, peer);
    }
}
