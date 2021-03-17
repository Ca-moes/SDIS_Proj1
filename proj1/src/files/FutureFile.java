package files;

import peer.Constants;
import peer.Peer;
import jobs.RestoreChunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureFile {
    private final String fileId;
    private final String pathname;
    private final Peer peer;
    private final int numChunks;
    private final List<SentChunk> sentChunks = new ArrayList<>();

    private String restoredPathname = "%s/restored_%s";

    public FutureFile(String fileId, String pathname, Peer peer) {
        this.fileId = fileId;
        this.pathname = pathname;
        this.peer = peer;

        for (String chunkId : this.peer.getInternalState().getSentChunksMap().keySet()) {
            String fId = chunkId.split("_")[0];
            if (fId.equals(fileId)) {
                sentChunks.add(this.peer.getInternalState().getSentChunksMap().get(chunkId));
            }
        }
        this.numChunks = sentChunks.size();
        this.restoredPathname = String.format(restoredPathname, this.peer.getInternalState().getPeerDirectory(), new File(pathname).getName());
    }

    public void restoreFile() {
        try {
            getChunks();
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

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
                System.out.println("[PEER] One or more chunks are missing!");
                return;
            }
            if (chunk.getChunkNo() != this.numChunks - 1 && chunk.getBody().length != Constants.CHUNK_SIZE) {
                System.out.println("[PEER] Received a chunk with less than 64KB but it was not the last chunk!");
                return;
            }

            System.out.printf("[PEER] Received Chunk - %s\n", chunk.getChunkId());

            randomAccessFile.seek(chunk.getChunkNo() * 64000L);
            randomAccessFile.write(chunk.getBody());

            chunk.clearBody();
        }
        randomAccessFile.close();

        System.out.printf("[PEER] %s RESTORED SUCCESSFULLY!\n", this.pathname);
    }
}
