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
            if (this.peer.getProtocolVersion().equals("1.0")) {
                this.peer.getInternalState().getSentChunksMap().get(message.getFileId() + "_" + message.getChunkNo()).setBody(message.getBody());
                System.out.printf("[RESTORE] Received %s : %d bytes\n", chunk.getChunkId(), message.getBody().length);
            } else {
                try {
                    chunk.loadBodyFromTCP(((ChunkMessage) message).getAddress(), ((ChunkMessage) message).getPort());
                } catch (IOException e) {
                    chunk.setConnectionFailed(true);
                    chunk.setReceivingData(false);
                    chunk.setBody(new byte[0]);
                    System.out.printf("[RESTORE] [TCP FAILED] %s\n", chunk.getChunkId());
                }
            }
        }
    }
}
