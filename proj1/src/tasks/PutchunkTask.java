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
        SavedChunk chunk = new SavedChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree(), message.getBody());

        Message reply = new StoredMessage(peer.getProtocolVersion(), peer.getPeerId(), message.getFileId(), message.getChunkNo(), message.getReplicationDegree());

        if (this.peer.getInternalState().getSavedChunks().contains(chunk)) {
            // This peer has this chunk but it will send a reply anyways cause it indicates that it has saved the chunk (UDP unreliability)
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 401));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            peer.getMulticastControl().sendMessage(reply);
        } else {
            // This peer will save the chunk locally
            peer.getInternalState().getSavedChunks().add(chunk);

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 401));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            peer.getMulticastControl().sendMessage(reply);
            peer.getInternalState().storeChunk(chunk);
            peer.getInternalState().commit();
        }
    }
}
