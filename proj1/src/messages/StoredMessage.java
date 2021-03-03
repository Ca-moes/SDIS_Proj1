package messages;

import peer.Peer;
import tasks.StoredTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;

public class StoredMessage extends Message {
    public StoredMessage(String protocolVersion, String senderId, String fileId, int chunkNo) {
        super(protocolVersion, "STORED", senderId, fileId, chunkNo, 0, new byte[0]);
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
        return new StoredTask(this, peer);
    }
}
