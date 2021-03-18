package jobs;

import files.SentChunk;
import messages.GetchunkMessage;
import messages.Message;
import peer.Peer;

import java.util.concurrent.Callable;

public class RestoreChunk implements Callable<SentChunk> {
    private final Peer peer;
    private final SentChunk chunk;

    public RestoreChunk(Peer peer, SentChunk chunk) {
        this.peer = peer;
        this.chunk = chunk;

        // just to be sure :)
        chunk.clearBody();
    }

    @Override
    public SentChunk call() throws Exception {
        Message message = new GetchunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), this.chunk.getFileId(), this.chunk.getChunkNo());

        int i = 1;
        do {
            this.peer.getMulticastControl().sendMessage(message);
            Thread.sleep(i* 1000L);
            if (!peer.getProtocolVersion().equals("1.0") && chunk.isReceivingData() && !chunk.connectionFailed()) {
                // wait just a bit longer
                Thread.sleep(1000);
            }
            i++;
        } while (chunk.getBody() == null && i < 10);

        return chunk;
    }
}
