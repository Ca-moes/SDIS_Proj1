package tasks;

import files.SentChunk;
import messages.ChunkMessage;
import peer.Peer;

import java.io.IOException;

/**
 * Task responsible to process a CHUNK Message
 */
public class ChunkTask extends Task {
    /**
     * @param message CHUNK message received on the multicast channel
     * @param peer    Peer responsible for this Task
     */
    public ChunkTask(ChunkMessage message, Peer peer) {
        super(message, peer);
    }

    /**
     * This method will process the CHUNK message associated Task
     * <p>
     * If this Peer is enhanced it will receive the Chunk body by TCP, otherwise the Chunk body is transmitted by
     * multicast. At the start it will mark the chunk as "already provided" if this peer has the chunk saved, meaning
     * this chunk is already provided, avoiding yet another CHUNK message to be sent by this peer in another thread.
     *
     * <strong>Restore Enhancement</strong>
     * <p>
     * This Task will be different depending on the version of the peer. If the peer is enhanced it will receive
     * on the body of the CHUNK message the IP Address and Port for the server socket created on the SendChunk Job,
     * it will then connect to the server and start receiving data, calling the loadBodyFromTCP on the target
     * chunk. The data will be transmitted with safety end to end. And there's no need to send the whole 64000B to
     * the multicast channel
     * </p>
     */
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
            } else {
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
