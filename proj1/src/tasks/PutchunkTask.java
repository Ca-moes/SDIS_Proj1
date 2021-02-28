package tasks;

import files.SavedChunk;
import messages.Message;
import messages.PutchunkMessage;
import messages.StoredMessage;
import peer.Peer;

import java.util.concurrent.ThreadLocalRandom;

public class PutchunkTask extends Task {
    public PutchunkTask(PutchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        System.out.println("Received PUTCHUNK");

        SavedChunk chunk = new SavedChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree(), 1, message.getBody());

        peer.getInternalState().getSavedChunks().add(chunk);

        Message reply = new StoredMessage(peer.getProtocolVersion(), peer.getPeerId(), message.getFileId(), message.getChunkNo(), message.getReplicationDegree());

        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 401));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peer.getMulticastControl().sendMessage(reply);

        peer.getInternalState().commit();
    }
}
