package jobs;

import files.SavedChunk;
import messages.ChunkMessage;
import messages.Message;
import peer.Peer;

public class SendChunk implements Runnable {
    private final SavedChunk chunk;
    private final Peer peer;

    public SendChunk(SavedChunk chunk, Peer peer) {
        this.chunk = chunk;
        this.peer = peer;
    }

    @Override
    public void run() {
        if (chunk.isAlreadyProvided()) {
            // System.out.println("[GETCHUNK] I've received a CHUNK message for this chunk so I won't provide it again");
            return;
        }
        if (chunk.getBody() == null) {
            // System.out.println("[GETCHUNK] Something happened and this chunk lost its body!");
            return;
        }

        Message message = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(), chunk.getBody());
        this.peer.getMulticastDataRestore().sendMessage(message);
        // no need to keep the body in memory
        chunk.clearBody();
        chunk.setBeingHandled(false);
        System.out.printf("[GETCHUNK] Sent %s!\n", chunk.getChunkId());
    }
}
