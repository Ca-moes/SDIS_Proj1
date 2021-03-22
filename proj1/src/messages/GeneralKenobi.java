package messages;

import peer.Peer;
import tasks.GeneralKenobiTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class GeneralKenobi extends Message {
    public GeneralKenobi(String protocolVersion, int senderId) {
        super(protocolVersion, "GENERALKENOBI", senderId, null, 0, 0, new byte[0]);
    }

    @Override
    public byte[] encodeToSend() {
        return String.format("%s %s %s \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ExecutorService getWorker(Peer peer) {
        return peer.getRequestsExecutor();
    }

    @Override
    public Task createTask(Peer peer) {
        return new GeneralKenobiTask(this, peer);
    }
}
