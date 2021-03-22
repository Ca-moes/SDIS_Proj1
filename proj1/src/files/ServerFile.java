package files;

import java.io.Serializable;

public class ServerFile implements Serializable {
    private final String pathname;
    private final String fileId;
    private final int replicationDegree;
    private final double size;

    public ServerFile(String pathname, String fileId, int replicationDegree, double size) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.replicationDegree = replicationDegree;
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public String getFileId() {
        return fileId;
    }

    public String getPathname() {
        return pathname;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    @Override
    public String toString() {
        return String.format("[ServerFile] Pathname: %s | FileID: %s | Replication Degree: %d | Size: %.2fKB", pathname, fileId, replicationDegree, size);
    }
}
