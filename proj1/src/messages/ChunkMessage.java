package messages;

import peer.Peer;
import tasks.ChunkTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;

public class ChunkMessage extends Message {
    public ChunkMessage(String protocolVersion, String senderId, String fileId, int chunkNo, byte[] body) {
        super(protocolVersion, "CHUNK", senderId, fileId, chunkNo, 0, body);
    }

    @Override
    public byte[] encodeToSend() {
        byte[] header = String.format("%s %s %s %s %d \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId,
                this.chunkNo).getBytes(StandardCharsets.UTF_8);

        byte[] toSend = new byte[header.length + this.body.length];
        System.arraycopy(header, 0, toSend, 0, header.length);
        System.arraycopy(this.body, 0, toSend, header.length, body.length);
        return toSend;
    }

    @Override
    public Task createTask(Peer peer) {
        return new ChunkTask(this, peer);
    }
}
