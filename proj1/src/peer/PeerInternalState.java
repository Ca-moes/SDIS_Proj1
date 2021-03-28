package peer;

import files.Chunk;
import files.SavedChunk;
import files.SentChunk;
import files.ServerFile;
import messages.Message;
import messages.RemovedMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Peer's Database and State Manager, this class is serializable as we need to store the data on persistent
 * memory so that we can reload the Peer without data loss. Our chunks maps are mapped ChunkId to Chunk where
 * ChunkID stands for fileID_chunkNo
 */
public class PeerInternalState implements Serializable {
    // chunkId -> sent chunk
    private final ConcurrentHashMap<String, SentChunk> sentChunksMap;
    // chunkId -> saved chunk
    private final ConcurrentHashMap<String, SavedChunk> savedChunksMap;
    private final ConcurrentHashMap<String, ServerFile> backedUpFilesMap;

    private final Set<String> deletedFiles;

    private static transient String PEER_DIRECTORY = "peer%d";
    private static transient String DB_FILENAME = "peer%d/data.ser";
    private static final transient String CHUNK_PATH = "%s/%s/%d";
    private long capacity = Constants.DEFAULT_CAPACITY;
    private long occupation;

    transient Peer peer;

    private static transient boolean acceptingRequests = true;

    /**
     * Default constructor for peer's internal state
     * <p>
     * It starts the necessary maps, we are using the ConcurrentHashMap class for everything in our project
     * has this program is highly competitive in therms of resources access
     *
     * @param peer Peer owning this database, this is useful for backwards access
     */
    public PeerInternalState(Peer peer) {
        this.sentChunksMap = new ConcurrentHashMap<>();
        this.savedChunksMap = new ConcurrentHashMap<>();
        this.backedUpFilesMap = new ConcurrentHashMap<>();
        this.deletedFiles = ConcurrentHashMap.newKeySet();
        this.peer = peer;
    }

    /**
     * Method to load the database from the local storage, or create a new one if it does not exist or cannot be
     * read. The peer is needed here so we can associate it with this database
     *
     * @param peer Peer owning this database
     * @return The PeerInternalState created/loaded
     */
    public static PeerInternalState loadInternalState(Peer peer) {
        PEER_DIRECTORY = String.format(PEER_DIRECTORY, peer.getPeerId());
        DB_FILENAME = String.format(DB_FILENAME, peer.getPeerId());

        PeerInternalState peerInternalState = null;

        try {
            FileInputStream inputStream = new FileInputStream(DB_FILENAME);
            ObjectInputStream objectIn = new ObjectInputStream(inputStream);
            peerInternalState = (PeerInternalState) objectIn.readObject();
            peerInternalState.peer = peer;
            inputStream.close();
            objectIn.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[PIS] Couldn't Load Database. Creating one now...");
        }

        if (peerInternalState == null) {
            // has been an error reading the peer internal state
            // meaning we need to create a new one
            peerInternalState = new PeerInternalState(peer);
        }

        peerInternalState.build();

        return peerInternalState;
    }

    /**
     * Method to build a new database
     */
    private void build() {
        File directory = new File(PEER_DIRECTORY);
        // create dir if it does not exist
        if (!directory.exists())
            if (!directory.mkdir()) {
                System.out.println("[PIS] Directory doesn't exist but could not be created");
                return;
            }
        try {
            new File(DB_FILENAME).createNewFile();
        } catch (IOException e) {
            System.out.println("[PIS] Could not load/create database file");
            e.printStackTrace();
            return;
        }
        this.updateOccupation();
        System.out.println("[PIS] Database Loaded/Created Successfully");
    }

    /**
     * Method to write the database to persistent memory, like a commit on a real database
     */
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

        this.updateOccupation();
    }

    /**
     * Method to update the Peer's Storage Occupation
     */
    private void updateOccupation() {
        this.occupation = this.calculateOccupation();
    }

    /**
     * Method to store a SavedChunk on the local storage
     *
     * @param chunk Chunk to be stored
     * @see SavedChunk
     */
    public void storeChunk(SavedChunk chunk) {
        try {
            String chunkPathName = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());

            Path path = Paths.get(chunkPathName);
            Files.createDirectories(path.getParent());

            Files.write(path, chunk.getBody());

            chunk.clearBody();

            updateOccupation();
        } catch (IOException i) {
            System.out.println("[PIS] Couldn't Save chunk " + chunk.getChunkId());
            i.printStackTrace();
        }

    }

    /**
     * Method to update Stored Confirmations on Sent Chunks Map
     *
     * @param chunk   Chunk to update confirmation
     * @param replier Peer who have stored the Chunk
     */
    public void updateStoredConfirmation(SentChunk chunk, int replier) {
        if (sentChunksMap.containsKey(chunk.getChunkId())) {
            sentChunksMap.get(chunk.getChunkId()).getPeers().add(replier);
        }
    }

    /**
     * Method to update Stored Confirmations on Saved Chunks Map
     *
     * @param chunk   Chunk to update confirmation
     * @param replier Peer who have stored the Chunk
     */
    public void updateStoredConfirmation(SavedChunk chunk, int replier) {
        if (savedChunksMap.containsKey(chunk.getChunkId())) {
            savedChunksMap.get(chunk.getChunkId()).getPeers().add(replier);
        }
    }

    /**
     * @return The sent chunks map
     */
    public ConcurrentHashMap<String, SentChunk> getSentChunksMap() {
        return sentChunksMap;
    }

    /**
     * @return The saved chunks map
     */
    public ConcurrentHashMap<String, SavedChunk> getSavedChunksMap() {
        return savedChunksMap;
    }

    /**
     * @return The backed up files map
     */
    public ConcurrentHashMap<String, ServerFile> getBackedUpFilesMap() {
        return backedUpFilesMap;
    }

    /**
     * @return This Peer's Internal State String representation
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(String.format("-------------- PEER %d REPORT --------------\n", peer.getPeerId()));
        ret.append("-- Backup Files --\n");
        for (Map.Entry<String, ServerFile> entry : this.backedUpFilesMap.entrySet()) {
            ServerFile file = entry.getValue();
            ret.append(file).append("\n");

            List<SentChunk> chunks = new ArrayList<>();

            for (Map.Entry<String, SentChunk> sentChunkEntry : this.sentChunksMap.entrySet()) {
                SentChunk chunk = sentChunkEntry.getValue();
                if (chunk.getFileId().equals(file.getFileId()))
                    chunks.add(chunk);
            }
            chunks.sort(Comparator.comparingInt(Chunk::getChunkNo));
            for (SentChunk chunk : chunks) {
                ret.append("\t").append(chunk).append("\n");
            }
        }
        ret.append("-- Saved Chunks --\n");
        for (Map.Entry<String, SavedChunk> savedChunkEntry : this.savedChunksMap.entrySet()) {
            SavedChunk chunk = savedChunkEntry.getValue();
            ret.append(chunk).append("\n");
        }
        ret.append("----- Storage -----").append("\n");
        ret.append(String.format("Capacity: %.2fKB\n", this.capacity / 1000.0));
        ret.append(String.format("Occupation: %.2fKB\n", this.occupation / 1000.0));
        ret.append("-------------- END OF REPORT --------------").append("\n");

        return ret.toString();
    }

    /**
     * This method will delete a chunk, delete empty folders and then update
     * the current occupation
     *
     * @param chunk Chunk to be deleted
     */
    public void deleteChunk(Chunk chunk) {
        String filepath = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());
        File file = new File(filepath);

        file.delete();

        this.deleteEmptyFolders();
        this.updateOccupation();
    }

    /**
     * Method to delete empty folders
     */
    private void deleteEmptyFolders() {
        try {
            Files.walk(Paths.get(PEER_DIRECTORY))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(File::isDirectory)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to remove entries on the sent chunks map related to a backed up file pathname
     *
     * @param pathname File's pathname whose chunks will be removed from the sent chunks map
     */
    public void deleteBackedUpEntries(String pathname) {
        String fileId = this.backedUpFilesMap.remove(pathname).getFileId();
        for (Map.Entry<String, SentChunk> entry : this.sentChunksMap.entrySet()) {
            SentChunk chunk = entry.getValue();
            if (chunk.getFileId().equals(fileId)) {
                this.sentChunksMap.remove(entry.getKey());
            }
        }
        this.commit();
    }

    /**
     * @return The Deleted Files Set
     */
    public Set<String> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * Method to fill a Chunk's Body from the local storage, it's using Java's Non-Blocking IO, so no problem
     * with thread safety here
     *
     * @param chunk Chunk whose body will be filled
     */
    public void fillBodyFromDisk(Chunk chunk) {
        if (chunk != null && chunk.getBody() == null) {
            String filepath = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());
            File file = new File(filepath);
            try {
                chunk.setBody(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                chunk.setBody(null);
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to force a space reclaiming, it will start by deleting safe chunks (chunks whose replication degree is
     * higher than the desired) and then if necessary it will delete the unsafe chunks, until either the occupation
     * is lesser or equal than the capacity or there are no more chunks left to delete. Either way, when this method
     * is called it's expected to also have interrupted the PUTCHUNK task handling for 60 seconds, leaving enough time
     * for the successful deletion of chunks
     */
    public void forceFreeSpace() {
        ArrayList<SavedChunk> safeDeletions = new ArrayList<>();
        ArrayList<SavedChunk> unsafeDeletions = new ArrayList<>();
        for (Map.Entry<String, SavedChunk> entry : this.getSavedChunksMap().entrySet()) {
            SavedChunk chunk = entry.getValue();
            if (chunk.getPeers().size() > chunk.getReplicationDegree())
                safeDeletions.add(chunk);
            else
                unsafeDeletions.add(chunk);
        }

        while (this.occupation > this.capacity && !safeDeletions.isEmpty()) {
            SavedChunk chunk = safeDeletions.remove(0);

            // System.out.printf("[PEER] Safe deleting %s\n", chunk.getChunkId());
            this.removeChunk(chunk);
        }
        System.out.printf("[PIS] Occupation after safe deleting: %d\n", this.occupation);
        while (this.occupation > this.capacity && !unsafeDeletions.isEmpty()) {
            SavedChunk chunk = unsafeDeletions.remove(0);

            // System.out.printf("[PEER] Unsafe deleting %s\n", chunk.getChunkId());
            this.removeChunk(chunk);
        }
        System.out.printf("[PIS] Occupation after unsafe deleting: %d\n", this.occupation);
    }

    /**
     * Method to remove a chunk from the file system and from the saved chunks map
     * <p>
     * This method will permanently delete the chunk and send a REMOVED message after that, it's used on the
     * reclaim protocol mostly, but can be used on the PUTCHUNK task if the Peer has not enough space to
     * store a chunk but can free some space by deleting safe chunks (Chunks whose actual replication degree is
     * actually higher than the desired)
     *
     * @param chunk Chunk to be Deleted
     */
    private void removeChunk(Chunk chunk) {
        this.deleteChunk(chunk);
        this.getSavedChunksMap().remove(chunk.getChunkId());
        this.commit();
        Message message = new RemovedMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
        this.peer.getMulticastControl().sendMessage(message);
    }

    /**
     * Method to free space by deleting unnecessary chunks (Chunks whose actual replication degree is actually higher
     * than the desired) it will lock PUTCHUNK requests while performing this task
     *
     * @return <code>true</code> if the operation finished successfully
     */
    public boolean freeSpace() {
        if (!acceptingRequests) return false;
        lockRequests(false);
        System.out.println("[PIS] Trying to free some space...");

        ArrayList<SavedChunk> safeDeletions = new ArrayList<>();
        for (Map.Entry<String, SavedChunk> entry : this.getSavedChunksMap().entrySet()) {
            SavedChunk chunk = entry.getValue();
            if (chunk.getPeers().size() > chunk.getReplicationDegree())
                safeDeletions.add(chunk);
        }

        while (!safeDeletions.isEmpty()) {
            SavedChunk chunk = safeDeletions.remove(0);

            System.out.printf("[PIS] Safe deleting %s\n", chunk.getChunkId());
            this.deleteChunk(chunk);
            this.getSavedChunksMap().remove(chunk.getChunkId());
            this.commit();

            //! Apparently we do not have to send a message if we are removing chunks to clear space for new chunks
            // Message message = new RemovedMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
            // this.peer.getMulticastControl().sendMessage(message);
        }
        lockRequests(true);
        return true;
    }

    /**
     * Method to interrupt the PUTCHUNK Tasks handling, this Peer will not handle any PUTCHUNK message for 60 seconds,
     * it's most likely due to the start of a reclaim operation.
     */
    public void interruptPutchunks() {
        this.setAcceptingRequests(false);
    }

    /**
     * Method to set the accepting requests flag, if it receives a <code>false</code> value as parameter it will
     * lock the requests and unlock them after 60 seconds
     *
     * @param acceptingRequests Flag's new Value
     */
    private void setAcceptingRequests(boolean acceptingRequests) {
        PeerInternalState.acceptingRequests = acceptingRequests;
        Timer timer = new Timer(true);

        if (acceptingRequests) {
            System.out.println("[PIS] Peer is now accepting PUTCHUNKS");
            timer.cancel();
        } else {
            System.out.println("[PIS] Peer is not accepting requests as of this moment, it will be available in 60 seconds");
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    setAcceptingRequests(true);
                }
            };
            timer.schedule(timerTask, 60 * 1000);
        }
    }

    /**
     * Method to instantly lock/unlock the PUTCHUNK Tasks handling
     *
     * @param accepting New value for the flag
     */
    private void lockRequests(boolean accepting) {
        acceptingRequests = accepting;
    }

    /**
     * @return <code>true</code> if this peer is receiving PUTCHUNK requests at the moment
     */
    public boolean isAcceptingRequests() {
        return acceptingRequests;
    }

    /**
     * Method to calculate the occupation if this peer
     *
     * @return The size in bytes used to backup chunks or -1 if there's any error
     */
    public long calculateOccupation() {
        try {
            return directorySize(new File(PEER_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Method to set a new capacity for this peer to use
     *
     * @param capacity New value for this peer's capacity
     */
    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    /**
     * @return This peer's current occupation
     */
    public long getOccupation() {
        return occupation;
    }

    /**
     * @return This peer's current capacity
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * @return This peer's file system directory
     */
    public String getPeerDirectory() {
        return PEER_DIRECTORY;
    }

    /**
     * Method to calculate a directory size in bytes using Java's NIO walker
     *
     * @param dir Directory used to calculate the size
     * @return The size in bytes for the directory passed as parameter
     * @throws IOException On error walking the tree
     */
    public long directorySize(File dir) throws IOException {
        Path folder = dir.toPath();
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }
}
