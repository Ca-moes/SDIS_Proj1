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

        for (Map.Entry<String, SentChunk> entry : this.peer.getInternalState().getSentChunksMap().entrySet()) {
            if (entry.getValue().getFileId().equals(fileId)) {
                sentChunks.add(entry.getValue());
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

            randomAccessFile.seek(chunk.getChunkNo() * 64000L);
            randomAccessFile.write(chunk.getBody());

            chunk.clearBody();
        }
        randomAccessFile.close();

        System.out.printf("[PEER] %s RESTORED SUCCESSFULLY!\n", this.pathname);
    }
}
