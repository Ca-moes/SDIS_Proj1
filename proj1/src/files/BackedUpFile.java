package files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class is responsible to store some info about a file which will be useful
 * during the backup of said file. Also contains a stream so we can read the file
 * in chunks of 64KB.
 */
public class BackedUpFile {
    private final String fileID;
    private final String pathname;
    private final InputStream chunkStream;

    /**
     * This constructor creates a new input stream and also defines the unique file ID for
     * this file
     *
     * @param pathname File's pathname to be backed up
     * @throws IOException On error creating the file ID or the FileInputStream
     * @see IOUtils
     */
    public BackedUpFile(String pathname) throws IOException {
        this.pathname = pathname;
        this.chunkStream = new FileInputStream(pathname);
        this.fileID = IOUtils.getFileId(pathname);
    }

    /**
     * Method to get the next chunk available, or null otherwise this is helpful as
     * we don't want to read a 64GB file into RAM, so we read chunk by chunk
     *
     * @return The byte array containing the chunk data or null if there is no more data
     * @throws IOException On error reading the file from the stream
     */
    public byte[] getNextChunk() throws IOException {
        byte[] myBuffer = new byte[64000];
        // the size in bytes is the length of the chunk / 2
        int size;
        if ((size = this.chunkStream.read(myBuffer, 0, 64000)) != -1) {
            return Arrays.copyOfRange(myBuffer, 0, size);
        }
        return null;
    }

    /**
     * @return This BackedUpFile file ID
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @return This BackedUpFile pathname
     */
    public String getPathname() {
        return pathname;
    }
}
