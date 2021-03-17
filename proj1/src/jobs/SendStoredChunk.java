package jobs;

import files.Chunk;
import files.SavedChunk;
import messages.Message;
import peer.Peer;

public class SendStoredChunk implements Runnable {
    private final Chunk chunk;
    private final Peer peer;
    private final Message message;

    public SendStoredChunk(Chunk chunk, Peer peer, Message message) {
        this.chunk = chunk;
        this.peer = peer;
        this.message = message;
    }

    @Override
    public void run() {
        // System.out.println("[DEBUG] SEND STORED CHUNK STARTED!");
        if (chunk.isStored() && peer.getInternalState().getSavedChunksMap().containsKey(chunk.getChunkId())) {
            // peer has this chunk stored and it will send a stored anyways
            peer.getMulticastControl().sendMessage(message);
        } else if (!peer.getInternalState().getSentChunksMap().containsKey(chunk.getChunkId())) {
            if (chunk.getPeers().size() < chunk.getReplicationDegree()) {
                if (chunk.getBody().length + this.peer.getInternalState().getOccupation() < this.peer.getInternalState().getCapacity()) {
                    // This peer will save the chunk locally
                    peer.getMulticastControl().sendMessage(message);
                    peer.getInternalState().storeChunk((SavedChunk) chunk);
                    chunk.setStored(true);
                    chunk.getPeers().add(peer.getPeerId());
                    peer.getInternalState().commit();
                    System.out.printf("[PUTCHUNK] Saved chunk %s\n", chunk.getChunkId());
                } else {
                    System.out.printf("[PEER] Not enough space for %s\n", chunk.getChunkId());
                }
            } else {
                // no need to backup here as it is already being backed up and it wont reply with STORED
                peer.getInternalState().getSavedChunksMap().remove(chunk.getChunkId());
            }
        }
        chunk.setReceivedPutchunk(false);
    }
}
