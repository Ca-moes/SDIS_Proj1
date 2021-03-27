package messages;

import peer.Peer;
import tasks.GetchunkTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

/**
 * GETCHUNK Message Data Class
 */
public class GetchunkMessage extends Message {
    //! Not documented
    public GetchunkMessage(String protocolVersion, int senderId, String fileId, int chunkNo) {
        super(protocolVersion, "GETCHUNK", senderId, fileId, chunkNo, 0, new byte[0]);
    }

    //! Not documented
    @Override
    public Task createTask(Peer peer) {
        return new GetchunkTask(this, peer);
    }

    //! Not documented
    @Override
    public byte[] encodeToSend() {
        return String.format("%s %s %s %s %d \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId,
                this.chunkNo).getBytes(StandardCharsets.UTF_8);
    }

    //! Not documented
    @Override
    public ExecutorService getWorker(Peer peer) {
        return peer.getRequestsExecutor();
    }
}
