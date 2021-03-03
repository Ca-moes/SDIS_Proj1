package tasks;

import files.SavedChunk;
import messages.Message;
import messages.PutchunkMessage;
import messages.StoredMessage;
import peer.Peer;

public class PutchunkTask extends Task {
    public PutchunkTask(PutchunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void start() {
        SavedChunk chunk = new SavedChunk(message.getFileId(), message.getChunkNo(), message.getReplicationDegree(), message.getBody());

        Message reply = new StoredMessage(peer.getProtocolVersion(), peer.getPeerId(), message.getFileId(), message.getChunkNo());

        if (this.peer.getInternalState().getSavedChunksMap().containsKey(chunk.getChunkId())) {
            // This peer has this chunk but it will send a reply anyways cause it indicates that it has saved the chunk (UDP unreliability)
            sleep();
            peer.getMulticastControl().sendMessage(reply);
        } else {
            peer.getInternalState().getSavedChunksMap().put(chunk.getChunkId(), chunk);

            sleep();

            if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
                // This peer will save the chunk locally
                peer.getMulticastControl().sendMessage(reply);
                peer.getInternalState().storeChunk(chunk);
                peer.getInternalState().commit();
            } else {
                // no need to backup here as it is already being backed up and it wont reply with STORED
                peer.getInternalState().getSavedChunksMap().remove(chunk.getChunkId());
            }
        }
    }
}
