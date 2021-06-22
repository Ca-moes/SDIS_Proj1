package files;

import java.io.Serializable;

/**
 * Data class to keep the Backed Up file data
 */
public class ServerFile implements Serializable {
    private final String pathname;
    private final String fileId;
    private final int replicationDegree;
    private final double size;

    /**
     * Constructor for this ServerFile given the pathname, fileId, replication degree and size
     *
     * @param pathname          File's Pathname
     * @param fileId            File's ID
     * @param replicationDegree File's Replication Degree
     * @param size              File's Size in KB
     */
    public ServerFile(String pathname, String fileId, int replicationDegree, double size) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.replicationDegree = replicationDegree;
        this.size = size;
    }

    /**
     * @return This File's Size
     */
    public double getSize() {
        return size;
    }

    /**
     * @return This File's ID
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * @return This File's Pathname
     */
    public String getPathname() {
        return pathname;
    }

    /**
     * @return This File's Pathname
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * @return Pretty Printed Information about this ServerFile
     */
    @Override
    public String toString() {
        return String.format("[ServerFile] Pathname: %s | FileID: %s | Replication Degree: %d | Size: %.2fKB", pathname, fileId, replicationDegree, size);
    }
}
