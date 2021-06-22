package files;

import jobs.RestoreChunk;
import peer.Constants;
import peer.Peer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class responsible to receive and reconstruct a "future" file
 */
public class FutureFile {
    private final String fileId;
    private final String pathname;
    private final Peer peer;
    private final int numChunks;
    private final List<SentChunk> sentChunks = new ArrayList<>();

    private String restoredPathname = "restored_%s";

    /**
     * Constructor for the FutureFile given a file Id, a pathname and the responsible peer
     *
     * @param fileId   This file's File ID
     * @param pathname This file's Pathname
     * @param peer     This file's responsible Peer
     * @see Peer
     */
    public FutureFile(String fileId, String pathname, Peer peer) {
        this.fileId = fileId;
        this.pathname = pathname;
        this.peer = peer;

        for (Map.Entry<String, SentChunk> entry : this.peer.getInternalState().getSentChunksMap().entrySet()) {
            if (entry.getValue().getFileId().equals(fileId)) {
                sentChunks.add(entry.getValue());
            }
        }

        this.numChunks = sentChunks.size();
        this.restoredPathname = String.format(restoredPathname, new File(pathname).getName());
    }

    /**
     * Access Method to restore a file
     */
    public void restoreFile() {
        try {
            getChunks();
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private method to receive the Chunks
     *
     * @throws ExecutionException   On error getting a future Chunk
     * @throws InterruptedException On error while sleeping
     * @throws IOException          On error while performing I/O operations
     */
    private void getChunks() throws ExecutionException, InterruptedException, IOException {
        System.out.println("[PEER] Gathering chunks from peers. Listing properties below...");
        System.out.printf("FileID: %s\n", this.fileId);
        System.out.printf("Pathname: %s\n", this.pathname);
        System.out.printf("Number of chunks: %d\n", this.numChunks);
        System.out.println("--------------------------------------------------------------------------");

        List<Future<SentChunk>> promisedChunks = new ArrayList<>();
        for (SentChunk chunk : this.sentChunks) {
            promisedChunks.add(this.peer.getIOExecutor().submit(new RestoreChunk(peer, chunk)));
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(this.restoredPathname), "rw");

        for (Future<SentChunk> promised : promisedChunks) {
            // waits here for the respective promise to be fulfilled
            SentChunk chunk = promised.get();

            if (chunk == null || chunk.getBody() == null) {
                System.out.println("[PEER] One or more chunks are missing! Aborting...");
                randomAccessFile.close();
                return;
            }
            if (chunk.getChunkNo() != this.numChunks - 1 && chunk.getBody().length != Constants.CHUNK_SIZE) {
                System.out.println("[PEER] Received a chunk with less than 64KB but it was not the last chunk! Aborting...");
                randomAccessFile.close();
                chunk.clearBody();
                return;
            }

            randomAccessFile.seek(chunk.getChunkNo() * 64000L);
            randomAccessFile.write(chunk.getBody());

            chunk.clearBody();
        }
        randomAccessFile.close();

        System.out.printf("[PEER] %s RESTORED SUCCESSFULLY!\n", this.pathname);
    }
}
