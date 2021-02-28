package peer;

import files.SavedChunk;
import files.SentChunk;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInternalState implements Serializable {
    private final ConcurrentHashMap<SentChunk, List<String>> sentChunksMap;
    private final HashSet<SavedChunk> savedChunks;
    private static transient String PEER_DIRECTORY = "%s";
    private static transient String DB_FILENAME = "%s/data.ser";

    public PeerInternalState() {
        this.sentChunksMap = new ConcurrentHashMap<>();
        this.savedChunks = new HashSet<>();
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
            e.printStackTrace();
        }

        if (peerInternalState == null) {
            // has been an error reading the peer internal state
            // meaning we need to create a new one
            peerInternalState = new PeerInternalState();
        }

        peerInternalState.build();

        System.out.println(peerInternalState);

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
        System.out.println("[PIS] - Saved Database: " + this);
    }

    public void updateBackedUpChunks(SentChunk chunk, String replier) {
        if (sentChunksMap.containsKey(chunk)) {
            sentChunksMap.get(chunk).add(replier);
        }
    }

    public ConcurrentHashMap<SentChunk, List<String>> getSentChunksMap() {
        return sentChunksMap;
    }

    public HashSet<SavedChunk> getSavedChunks() {
        return savedChunks;
    }

    @Override
    public String toString() {
        return "PeerInternalState{" +
                "\nsentChunksMap=" + sentChunksMap +
                ",\nsavedChunks=" + savedChunks +
                '}';
    }
}
