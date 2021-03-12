package tasks;

import files.SavedChunk;
import messages.ChunkMessage;
import messages.GetchunkMessage;
import messages.Message;
import peer.Peer;

public class GetchunkTask extends Task {
    public GetchunkTask(GetchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        if (!this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            System.out.printf("[GETCHUNK] I dont have that chunk! %s\n", message.getFileId() + "_" + message.getChunkNo());
            return;
        }

        SavedChunk chunk = this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
        this.peer.getInternalState().fillBodyFromDisk(chunk);

        if (chunk.getBody() == null) {
            System.out.println("[GETCHUNK] I was supposed to have that chunk, but I don't!");
            return;
        }

        // this peer is already handling this chunk
        if (chunk.isBeingHandled()) {
            return;
        }

        chunk.setBeingHandled(true);
        chunk.setAlreadyProvided(false);

        sleep();

        if (chunk.isAlreadyProvided()) {
            System.out.println("[GETCHUNK] I've received a CHUNK message for this chunk so I won't provide it again");
            return;
        }
        if (chunk.getBody() == null) {
            System.out.println("[GETCHUNK] Something happened and this chunk lost its body!");
            return;
        }

        Message message = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(), chunk.getBody());
        this.peer.getMulticastDataRestore().sendMessage(message);
        // no need to keep the body in memory
        chunk.clearBody();
        chunk.setBeingHandled(false);
        System.out.println("[GETCHUNK] Sent a chunk!");
    }
}
