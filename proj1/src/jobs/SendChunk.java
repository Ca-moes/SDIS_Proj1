package jobs;

import files.SavedChunk;
import messages.ChunkMessage;
import messages.GetchunkMessage;
import messages.Message;
import peer.Peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Job responsible to Send a Chunk to the initiator peer, this job will be triggered by the GETCHUNK task
 *
 * @see tasks.GetchunkTask
 */
public class SendChunk implements Runnable {
    private final GetchunkMessage message;
    private final SavedChunk chunk;
    private final Peer peer;

    /**
     * @param message GETCHUNK message, the message is need to check if the data feed is done by TCP or Multicast
     * @param chunk   Chunk to be sent
     * @param peer    Peer responsible for this Job
     * @see GetchunkMessage
     * @see tasks.GetchunkTask
     */
    public SendChunk(GetchunkMessage message, SavedChunk chunk, Peer peer) {
        this.message = message;
        this.chunk = chunk;
        this.peer = peer;
    }

    /**
     * Method to start this job, this method will perform the necessary checks and then send the chunk by Multicast or
     * TCP, depending on the protocol version
     */
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

        Message message;
        if (!this.peer.isEnhanced() || !this.message.isEnhanced()) {
            message = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(), chunk.getBody());
            this.peer.getMulticastDataRestore().sendMessage(message);
            // no need to keep the body in memory
            int bytes = chunk.getBody().length;
            chunk.clearBody();
            chunk.setBeingHandled(false);
            System.out.printf("[GETCHUNK] Sent %s : %d bytes\n", chunk.getChunkId(), bytes);
        } else {
            try {
                ServerSocket serverSocket = new ServerSocket(0);
                message = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(),
                        Message.addressPortToBytes(peer.getAddress(), serverSocket.getLocalPort()));
                this.peer.getMulticastDataRestore().sendMessage(message);

                serverSocket.setSoTimeout(2000);
                Socket connection = serverSocket.accept();
                serverSocket.close();

                DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                stream.write(chunk.getBody(), 0, chunk.getBody().length);
                stream.flush();
                stream.close();
                chunk.clearBody();
                chunk.setBeingHandled(false);
                System.out.printf("[GETCHUNK] [TCP] Sent %s\n", chunk.getChunkId());
            } catch (IOException e) {
                System.out.printf("[GETCHUNK] [TCP] Failed for chunk: %s\nFalling back to vanilla protocol...\n", chunk.getChunkId());
                message = new ChunkMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo(), chunk.getBody());
                this.peer.getMulticastDataRestore().sendMessage(message);
                // no need to keep the body in memory
                int bytes = chunk.getBody().length;
                chunk.clearBody();
                chunk.setBeingHandled(false);
                System.out.printf("[GETCHUNK] Sent %s : %d bytes\n", chunk.getChunkId(), bytes);
            }
        }
    }
}
