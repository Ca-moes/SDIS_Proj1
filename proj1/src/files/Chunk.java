package files;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract Super Class for Chunk information and logic
 * <p>
 * This class is serializable to help writing to the database any relevant information,
 * as we are already saving the chunks on the file system, the field <code>body</code> is
 * set to be <code>transient</code>
 *
 * @see SentChunk
 * @see SavedChunk
 */
public abstract class Chunk implements Serializable {
    protected String fileId;
    protected int chunkNo;
    protected int replicationDegree;
    protected final Set<Integer> peers;
    protected transient byte[] body;
    protected boolean receivedPutchunk = false;
    private boolean stored = false;
    private double size = 0;

    /**
     * @return True if the peer have already received a PUTCHUNK for this Chunk
     */
    public boolean receivedPutchunk() {
        return receivedPutchunk;
    }

    /**
     * Set the received putchunk flag
     *
     * @param receivedPutchunk New value to be set on the received putchunk flag
     */
    public void setReceivedPutchunk(boolean receivedPutchunk) {
        this.receivedPutchunk = receivedPutchunk;
    }

    /**
     * Constructor for a Chunk given a file ID and a chunk Number
     *
     * @param fileId  Chunk's File Id
     * @param chunkNo Chunk's sequential number
     */
    public Chunk(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = 0;
        this.peers = ConcurrentHashMap.newKeySet();
    }

    /**
     * Construction for a Chunk given the full information of said Chunk
     *
     * @param fileId            Chunk's File ID
     * @param chunkNo           Chunk's Sequential Number
     * @param replicationDegree Chunk's Desired Replication Degree
     * @param body              Chunk's Body
     */
    public Chunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.peers = ConcurrentHashMap.newKeySet();
        this.body = body;
    }

    /**
     * Construction for a Chunk given the full information of said Chunk (except the body)
     *
     * @param fileId            Chunk's File ID
     * @param chunkNo           Chunk's Sequential Number
     * @param replicationDegree Chunk's Desired Replication Degree
     */
    public Chunk(String fileId, int chunkNo, int replicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.peers = ConcurrentHashMap.newKeySet();
    }

    /**
     * A chunks is the same as other chunk if the file ID and the chunk number is the same
     *
     * @param o Other chunk to be tested the equality
     * @return true if the chunks are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return chunkNo == chunk.chunkNo && Objects.equals(fileId, chunk.fileId);
    }

    /**
     * Method to set the body bytes on the chunk
     *
     * @param body byte array containing the data information
     */
    public void setBody(byte[] body) {
        this.body = body;
        if (body != null) {
            this.size = body.length / 1000.0;
        }
    }

    /**
     * @return The body byte array containing this chunk's data
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Method to clear the body, we don't always need to keep the data in cache, so
     * we set the body to <code>null</code>
     */
    public void clearBody() {
        this.body = null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, chunkNo);
    }

    /**
     * @return This Chunk's sequential number
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * @return This Chunk's File ID
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * The Chunk ID is represented with fileID_chunkNo
     *
     * @return This Chunk's ID
     */
    public String getChunkId() {
        return fileId + "_" + chunkNo;
    }

    /**
     * @return The desired replication degree
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * @return The peers storing this Chunk (i.e. The peers who sent a STORED message for this Chunk)
     */
    public Set<Integer> getPeers() {
        return peers;
    }

    /**
     * @return True if this peer is saving this Chunk
     */
    public boolean isStored() {
        return this.stored;
    }

    /**
     * Setter Method for the stored flag
     *
     * @param stored Value to set the stored flag
     */
    public void setStored(boolean stored) {
        this.stored = stored;
    }

    /**
     * @return The Chunk's Size in Byte
     */
    public double getSize() {
        return size;
    }
}
