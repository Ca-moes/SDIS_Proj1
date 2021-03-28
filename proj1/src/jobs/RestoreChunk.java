package jobs;

import files.SentChunk;
import messages.GetchunkMessage;
import messages.Message;
import peer.Peer;

import java.util.concurrent.Callable;

/**
 * Job responsible for Restoring a Chunk and making sure the data is received
 */
public class RestoreChunk implements Callable<SentChunk> {
    private final Peer peer;
    private final SentChunk chunk;

    /**
     * @param peer  Peer responsible for the Restoration Job
     * @param chunk Chunk to be filled with body data
     */
    public RestoreChunk(Peer peer, SentChunk chunk) {
        this.peer = peer;
        this.chunk = chunk;

        // just to be sure :)
        chunk.clearBody();
    }

    /**
     * Call method to return the chunk once it's filled or once it number of tries are exceeded
     *
     * @return The chunk sent for restoration
     * @throws Exception On thread sleep interruption
     * @see GetchunkMessage
     * @see tasks.GetchunkTask
     */
    @Override
    public SentChunk call() throws Exception {
        Message message = new GetchunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), this.chunk.getFileId(), this.chunk.getChunkNo());

        int i = 1;
        do {
            this.peer.getMulticastControl().sendMessage(message);
            Thread.sleep(i * 1000L);
            if (peer.isEnhanced() && chunk.isReceivingData() && !chunk.connectionFailed()) {
                // wait just a bit longer
                Thread.sleep(1000);
            }
            i++;
        } while (chunk.getBody() == null && i < 10);

        return chunk;
    }
}
