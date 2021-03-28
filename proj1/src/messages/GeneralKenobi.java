package messages;

import peer.Peer;
import tasks.GeneralKenobiTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

/**
 * GENERALKENOBI Message Data Class
 */
public class GeneralKenobi extends Message {
    //! Not documented
    public GeneralKenobi(String protocolVersion, int senderId) {
        super(protocolVersion, "GENERALKENOBI", senderId, null, 0, 0, new byte[0]);
    }

    //! Not documented
    @Override
    public byte[] encodeToSend() {
        return String.format("%s %s %s \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId).getBytes(StandardCharsets.UTF_8);
    }

    //! Not documented
    @Override
    public ExecutorService getWorker(Peer peer) {
        return peer.getRequestsExecutor();
    }

    //! Not documented
    @Override
    public Task createTask(Peer peer) {
        return new GeneralKenobiTask(this, peer);
    }
}
