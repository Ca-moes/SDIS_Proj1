package messages;

import peer.Peer;
import tasks.PutchunkTask;
import tasks.Task;

import java.util.concurrent.ExecutorService;

/**
 * PUTCHUNK Message Data Class
 */
public class PutchunkMessage extends Message {
    //! Not documented
    public PutchunkMessage(String protocolVersion, int senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(protocolVersion, "PUTCHUNK", senderId, fileId, chunkNo, replicationDegree, body);
    }

    //! Not documented
    @Override
    public byte[] encodeToSend() {
        byte[] header = super.encodeToSend();

        byte[] toSend = new byte[header.length + this.body.length];
        System.arraycopy(header, 0, toSend, 0, header.length);
        System.arraycopy(this.body, 0, toSend, header.length, body.length);
        return toSend;
    }

    //! Not documented
    @Override
    public Task createTask(Peer peer) {
        return new PutchunkTask(this, peer);
    }

    //! Not documented
    @Override
    public ExecutorService getWorker(Peer peer) {
        return peer.getRequestsExecutor();
    }
}
