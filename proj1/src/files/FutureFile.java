package files;

import peer.Constants;
import peer.Peer;
import tasks.RestoreChunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureFile {
    private final String fileId;
    private final String pathname;
    private final Peer peer;
    private final int numChunks;
    private final List<SentChunk> sentChunks = new ArrayList<>();
    private final List<SentChunk> filledChunks = new ArrayList<>();

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

        for (Future<SentChunk> promised : promisedChunks) {
            SentChunk chunk = promised.get();
            // waits here for the respective promise to be fulfilled
            if (chunk == null || chunk.getBody() == null) {
                System.out.println("[PEER] One or more chunks are missing!");
                return;
            }
            if (chunk.getChunkNo() != this.numChunks - 1 && chunk.getBody().length != Constants.CHUNK_SIZE) {
                System.out.println("[PEER] Received a chunk with less than 64KB but it was not the last chunk!");
                return;
            }
            System.out.printf("[PEER] Received Chunk - %s\n", chunk.getChunkId());
            // All is good, add chunk to the chunks to be handled
            filledChunks.add(chunk);
            // store the chunk locally, we could theoretically get a 64GB file and
            // not all of us have a 64GB+ RAM PC :(
            peer.getInternalState().storeChunk(new SavedChunk(chunk));
            // so we need to clear the body as we dont need it in cache
            chunk.clearBody();
        }

        System.out.printf("[PEER] Gathered all chunks (%d) - Starting Reconstruction\n", filledChunks.size());

        // we did not guarantee the chunks were sorted, will do that now
        filledChunks.sort(Comparator.comparingInt(Chunk::getChunkNo));

        // read the file from the filesystem
        File restored = new File(this.restoredPathname);
        restored.getParentFile().mkdirs();
        restored.createNewFile();
        FileOutputStream stream = new FileOutputStream(restored, true);
        for (SentChunk chunk : filledChunks) {
            peer.getInternalState().fillBodyFromDisk(chunk);
            stream.write(chunk.getBody());
            peer.getInternalState().deleteChunk(chunk);
            chunk.clearBody();
        }
        stream.close();
        System.out.printf("[PEER] %s RESTORED SUCCESSFULLY!\n", this.pathname);
    }
}
