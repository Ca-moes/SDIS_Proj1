package files;

import java.io.Serializable;

public class SavedChunk extends Chunk implements Serializable {
    private transient byte[] body;
    private boolean isBeingHandled = false;
    private boolean alreadyProvided = false;

    public SavedChunk(String fileId, int chunkNo, int replicationDegree, byte[] body) {
        super(fileId, chunkNo, replicationDegree);
        this.body = body;
    }

    public SavedChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
        this.body = new byte[0];
    }

    public byte[] getBody() {
        return body;
    }

    public void clearBody() {
        body = null;
    }

    @Override
    public String toString() {
        return "\nSavedChunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                ", peers=" + peers +
                '}';
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }

    public void setAlreadyProvided(boolean alreadyProvided) {
        this.alreadyProvided = alreadyProvided;
    }

    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    public boolean isAlreadyProvided() {
        return alreadyProvided;
    }
}
