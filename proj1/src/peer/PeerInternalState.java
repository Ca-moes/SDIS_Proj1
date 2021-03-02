package peer;

import files.SavedChunk;
import files.SentChunk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInternalState implements Serializable {
    // chunkId -> sent chunk
    private final ConcurrentHashMap<String, SentChunk> sentChunksMap;
    // chunkId -> saved chunk
    private final ConcurrentHashMap<String, SavedChunk> savedChunksMap;
    private final ConcurrentHashMap<String, String> backedUpFilesMap;

    private static transient String PEER_DIRECTORY = "%s";
    private static transient String DB_FILENAME = "%s/data.ser";
    private static transient String CHUNK_PATH = "%s/%s/%d";

    private final transient Peer peer;

    public PeerInternalState(Peer peer) {
        this.sentChunksMap = new ConcurrentHashMap<>();
        this.savedChunksMap = new ConcurrentHashMap<>();
        this.backedUpFilesMap = new ConcurrentHashMap<>();
        this.peer = peer;
    }

    public static PeerInternalState loadInternalState(Peer peer) {
        PEER_DIRECTORY = String.format(PEER_DIRECTORY, peer.getPeerId());
        DB_FILENAME = String.format(DB_FILENAME, peer.getPeerId());

        PeerInternalState peerInternalState = null;

        try {
            FileInputStream inputStream = new FileInputStream(DB_FILENAME);
            ObjectInputStream objectIn = new ObjectInputStream(inputStream);
            peerInternalState = (PeerInternalState) objectIn.readObject();
            inputStream.close();
            objectIn.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[PIS] - Couldn't Load Database. Creating one now...");
        }

        if (peerInternalState == null) {
            // has been an error reading the peer internal state
            // meaning we need to create a new one
            peerInternalState = new PeerInternalState(peer);
        }

        peerInternalState.build();

        return peerInternalState;
    }

    private void build() {
        File directory = new File(PEER_DIRECTORY);
        // create dir if it does not exist
        if (!directory.exists())
            if (!directory.mkdir()) {
                System.out.println("[PIS] - Directory doesn't exist but could not be created");
                return;
            }
        try {
            new File(DB_FILENAME).createNewFile();
        } catch (IOException e) {
            System.out.println("[PIS] - Could not load/create database file");
            e.printStackTrace();
            return;
        }
        System.out.println("[PIS] - Database Loaded/Created Successfully");
    }

    public void commit() {
        try {
            FileOutputStream fileOut = new FileOutputStream(DB_FILENAME);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.flush();
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
//        System.out.println("[PIS] - Saved Database: " + this);
    }

    public void storeChunk(SavedChunk chunk) {
        try {
            String chunkPathName = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());

            Path path = Paths.get(chunkPathName);
            Files.createDirectories(path.getParent());

            FileOutputStream fos = new FileOutputStream(chunkPathName);
            fos.write(chunk.getBody());
            fos.close();

            chunk.getPeers().add(peer.getPeerId());
        } catch (IOException i) {
            System.out.println("[PIS] - Couldn't Save chunk " + chunk.getChunkId() + " on this peer");
            i.printStackTrace();
        }
    }

    public void updateStoredConfirmation(SentChunk chunk, String replier) {
        if (sentChunksMap.containsKey(chunk.getChunkId())) {
            sentChunksMap.get(chunk.getChunkId()).getPeers().add(replier);
        }
    }

    public void updateStoredConfirmation(SavedChunk chunk, String replier) {
        if (savedChunksMap.containsKey(chunk.getChunkId())) {
            savedChunksMap.get(chunk.getChunkId()).getPeers().add(replier);
        }
    }

    public ConcurrentHashMap<String, SentChunk> getSentChunksMap() {
        return sentChunksMap;
    }

    public ConcurrentHashMap<String, SavedChunk> getSavedChunksMap() {
        return savedChunksMap;
    }

    public ConcurrentHashMap<String, String> getBackedUpFilesMap() {
        return backedUpFilesMap;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("BACKED FILES MAP\n");
        out.append(this.backedUpFilesMap);

        out.append("\nSAVED CHUNKS MAP");
        for (String chunkId : this.savedChunksMap.keySet()) {
            out.append(this.savedChunksMap.get(chunkId));
        }
        out.append("\nSENT CHUNKS MAP");
        for (String chunkId : this.sentChunksMap.keySet()) {
            out.append(this.sentChunksMap.get(chunkId));
        }
        return out.toString();
    }
}
