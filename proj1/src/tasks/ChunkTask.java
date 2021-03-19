package tasks;

import files.SentChunk;
import messages.ChunkMessage;
import peer.Peer;

import java.io.IOException;

public class ChunkTask extends Task {
    public ChunkTask(ChunkMessage message, Peer peer) {
        super(message, peer);
    }

    @Override
    public void run() {
        if (this.peer.getInternalState().getSavedChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            // this chunk is stored here, so it will be marked as "already provided by another peer"
            this.peer.getInternalState().getSavedChunksMap().get(message.getFileId() + "_" + message.getChunkNo()).setAlreadyProvided(true);
        } else if (this.peer.getInternalState().getSentChunksMap().containsKey(message.getFileId() + "_" + message.getChunkNo())) {
            // this chunk is being retrieved for restoration
            SentChunk chunk = this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo());
            // if the message is not enhanced it will not use TCP because sender peer is not handling TCP connections
            if (!this.message.isEnhanced()) {
                chunk.setBody(message.getBody());
                System.out.printf("[RESTORE] Received %s : %d bytes from Peer%d\n", chunk.getChunkId(), message.getBody().length, message.getSenderId());
            }
            // if the peer is not enhanced but the message is, this peer is not handling TCP connections, and the protocol
            // must fallback to the default version of it
            else if (!this.peer.isEnhanced() && this.message.isEnhanced()) {
                chunk.setConnectionFailed(true);
                chunk.setReceivingData(false);
                chunk.setBody(null);
                System.out.printf("[RESTORE] Received an unsupported CHUNK message from Peer%d, trying to fallback to 1.0\nChunk: %s\n", message.getSenderId(), chunk.getChunkId());
            }
            else {
                try {
                    chunk.loadBodyFromTCP(((ChunkMessage) message).getAddress(), ((ChunkMessage) message).getPort());
                } catch (IOException e) {
                    chunk.setConnectionFailed(true);
                    chunk.setReceivingData(false);
                    chunk.setBody(null);
                    System.out.printf("[RESTORE] [TCP FAILED] %s\n", chunk.getChunkId());
                }
            }
        }
    }
}
