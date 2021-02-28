package peer;

import files.SavedChunk;
import files.SentChunk;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerInternalState {
    private final HashMap<String, List<SentChunk>> sentChunksMap;
    private final String savedChunksFilename;
    private List<SavedChunk> savedChunks = null;
    private final Peer peer;

    public PeerInternalState(Peer peer, String savedChunksFilename) {
        this.peer = peer;
        this.sentChunksMap = new HashMap<>();
        this.savedChunksFilename = savedChunksFilename;
    }

    public boolean loadSavedChunks() {
        try {
            FileInputStream fis = new FileInputStream("chunks.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.savedChunks = (ArrayList) ois.readObject();
            ois.close();
            fis.close();

            return true;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateSavedChunks(String chunkId) {
        // TODO
    }

    public boolean saveSavedChunks() {
        if (savedChunks != null) {
            try {
                FileOutputStream fos = new FileOutputStream(peer.getPeerId() + "/saved_chunks.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(this.savedChunks);
                oos.close();
                fos.close();
                return true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return false;
    }

    public HashMap<String, List<SentChunk>> getSentChunksMap() {
        return sentChunksMap;
    }

    public String getSavedChunksFilename() {
        return savedChunksFilename;
    }
}
