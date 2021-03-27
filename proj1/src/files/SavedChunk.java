package files;

import java.io.Serializable;

/**
 * SavedChunk class, this class is an extension of the Chunk meant to keep the saved chunks information
 *
 * @see Chunk
 */
public class SavedChunk extends Chunk implements Serializable {
    private boolean isBeingHandled = false;
    private boolean alreadyProvided = false;

    /**
     * Construction for a Saved Chunk given the full information of said Chunk
     *
     * @param fileId            Chunk's File ID
     * @param chunkNo           Chunk's Sequential Number
     * @param replicationDegree Chunk's Desired Replication Degree
     * @param body              Chunk's Body
     */
    public SavedChunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(fileId, chunkNo, replicationDegree);
        this.setBody(body);
    }

    /**
     * Constructor for a Saved Chunk given a file ID and a chunk Number
     *
     * @param fileId  Chunk's File Id
     * @param chunkNo Chunk's sequential number
     */
    public SavedChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
        this.body = new byte[0];
    }

    /**
     * Constructor for a Saved Chunk given a SentChunk information
     *
     * @param chunk Chunk information to be used to create a Saved Chunk
     */
    public SavedChunk(SentChunk chunk) {
        super(chunk.getFileId(), chunk.getChunkNo());
        this.body = chunk.getBody();
    }

    /**
     * @return Pretty Printed Saved Chunk information
     */
    @Override
    public String toString() {
        return String.format("[SavedChunk] FileID: %s | ChunkNo: %-4d | Desired Replication Degree: %d | Perceived Replication Degree: %d | Size: %.2fKB", fileId, chunkNo, replicationDegree, peers.size(), getSize());
    }

    //! Not documented
    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }

    //! Not documented
    public void setAlreadyProvided(boolean alreadyProvided) {
        this.alreadyProvided = alreadyProvided;
    }

    //! Not documented
    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    //! Not documented
    public boolean isAlreadyProvided() {
        return alreadyProvided;
    }
}
