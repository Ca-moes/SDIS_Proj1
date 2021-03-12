package files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BackedUpFile {
    private final String fileID;
    private final String pathname;
    private final InputStream chunkStream;

    public BackedUpFile(String pathname) throws IOException {
        this.pathname = pathname;
        this.chunkStream = new FileInputStream(pathname);
        this.fileID = IOUtils.getFileId(pathname);
    }

    public byte[] getNextChunk() throws IOException {
        byte[] myBuffer = new byte[64000];
        // the size in bytes is the length of the chunk / 2
        int size;
        if ((size = this.chunkStream.read(myBuffer,0,64000)) != -1) {
            return Arrays.copyOfRange(myBuffer, 0, size);
        }
        return null;
    }

    public String getFileID() {
        return fileID;
    }

    public String getPathname() {
        return pathname;
    }

}
