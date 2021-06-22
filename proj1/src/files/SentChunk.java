package files;

import peer.Constants;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * SentChunk class, this class is an extension of the Chunk meant to keep the sent chunks information
 *
 * @see Chunk
 */
public class SentChunk extends Chunk implements Serializable {
    /**
     * Construction for a Sent Chunk given the full information of said Chunk (except body)
     *
     * @param fileId            Chunk's File ID
     * @param chunkNo           Chunk's Sequential Number
     * @param replicationDegree Chunk's Desired Replication Degree
     */
    public SentChunk(String fileId, int chunkNo, int replicationDegree) {
        super(fileId, chunkNo, replicationDegree);
    }

    /**
     * Constructor for a Sent Chunk given a file ID and a chunk Number
     *
     * @param fileId  Chunk's File Id
     * @param chunkNo Chunk's sequential number
     */
    public SentChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
    }

    private boolean connectionFailed = false;
    private boolean receivingData = false;


    /**
     * <h2>Method to load this Chunk's body from a TCP connection</h2>
     * <p>
     * This method tries to establish a TCP connection to the server (i.e. the peer who sent the CHUNK message),
     * then it reads the body from the input stream using the newly created socket
     * </p>
     *
     * @param address Address used to create the client socket
     * @param port    Post used to create the client socket
     * @throws IOException On error reading the bytes from the stream
     * @see jobs.SendChunk
     */
    public void loadBodyFromTCP(InetAddress address, int port) throws IOException {
        Socket client = new Socket(address.getHostAddress(), port);

        receivingData = true;

        DataInputStream stream = new DataInputStream(client.getInputStream());
        int readBytes = 0;
        int lastRead = 1;
        byte[] aux = new byte[Constants.CHUNK_SIZE];
        while (readBytes < Constants.CHUNK_SIZE && lastRead >= 0) {
            lastRead = stream.read(aux, readBytes, Constants.CHUNK_SIZE - readBytes);
            readBytes += Math.max(lastRead, 0);
        }
        this.body = Arrays.copyOf(aux, readBytes);
        receivingData = false;

        System.out.printf("[RESTORE] [TCP] Received %s : %d bytes\n", getChunkId(), body.length);
    }

    //! Not documented
    public void setConnectionFailed(boolean connectionFailed) {
        this.connectionFailed = connectionFailed;
    }

    //! Not documented
    public boolean connectionFailed() {
        return connectionFailed;
    }

    /**
     * @return Pretty Printed Sent Chunk information
     */
    @Override
    public String toString() {
        return String.format("[SentChunk] ChunkNo: %-4d | Perceived Replication Degree: %d", chunkNo, peers.size());
    }

    //! Not documented
    public void setReceivingData(boolean receivingData) {
        this.receivingData = receivingData;
    }

    //! Not documented
    public boolean isReceivingData() {
        return receivingData;
    }
}
