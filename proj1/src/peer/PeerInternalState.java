package peer;

import files.Chunk;
import files.SavedChunk;
import files.SentChunk;
import messages.Message;
import messages.RemovedMessage;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PeerInternalState implements Serializable {
    // chunkId -> sent chunk
    private final ConcurrentHashMap<String, SentChunk> sentChunksMap;
    // chunkId -> saved chunk
    private final ConcurrentHashMap<String, SavedChunk> savedChunksMap;
    private final ConcurrentHashMap<String, String> backedUpFilesMap;

    private final Set<String> deletedFiles;

    private static transient String PEER_DIRECTORY = "peer%d";
    private static transient String DB_FILENAME = "peer%d/data.ser";
    private static transient String CHUNK_PATH = "%s/%s/%d";
    private long capacity = Constants.DEFAULT_CAPACITY;
    private long occupation;

    private final transient Peer peer;

    private transient boolean acceptingRequests = true;

    public PeerInternalState(Peer peer) {
        this.sentChunksMap = new ConcurrentHashMap<>();
        this.savedChunksMap = new ConcurrentHashMap<>();
        this.backedUpFilesMap = new ConcurrentHashMap<>();
        this.deletedFiles = ConcurrentHashMap.newKeySet();
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
        this.updateOccupation();
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

        this.updateOccupation();
    }

    private void updateOccupation() {
        this.occupation = this.calculateOccupation();
    }

    public void storeChunk(SavedChunk chunk) {
        try {
            String chunkPathName = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());

            Path path = Paths.get(chunkPathName);
            Files.createDirectories(path.getParent());

            FileOutputStream fos = new FileOutputStream(chunkPathName);
            fos.write(chunk.getBody());
            fos.close();

            chunk.clearBody();

            updateOccupation();
        } catch (IOException i) {
            System.out.println("[PIS] - Couldn't Save chunk " + chunk.getChunkId() + " on this peer");
            i.printStackTrace();
        }

    }

    public void updateStoredConfirmation(SentChunk chunk, int replier) {
        if (sentChunksMap.containsKey(chunk.getChunkId())) {
            sentChunksMap.get(chunk.getChunkId()).getPeers().add(replier);
        }
    }

    public void updateStoredConfirmation(SavedChunk chunk, int replier) {
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

        double numberOfExceeded = 0;

        for (String chunkId : this.sentChunksMap.keySet()) {
            out.append(this.sentChunksMap.get(chunkId));
            if (this.sentChunksMap.get(chunkId).getPeers().size() > this.sentChunksMap.get(chunkId).getReplicationDegree())
                numberOfExceeded += 1;
        }
        out.append("\nDELETED FILES HASHMAP\n");
        for (String fileId : this.deletedFiles) {
            out.append(fileId).append("\n");
        }

        out.append("CAPACITY: ").append(this.capacity / 1000.0).append("KB\n");
        out.append("OCCUPIED SPACE: ").append(this.occupation / 1000.0).append("KB\n");
        out.append("Exceeded Rate: ").append(numberOfExceeded / this.sentChunksMap.size()).append("\n");

        return out.toString();
    }

    public void deleteChunk(Chunk chunk) {
        String filepath = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());
        File file = new File(filepath);

        file.delete();

        this.deleteEmptyFolders();
        this.updateOccupation();
    }

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

    public void deleteBackedUpEntries(String pathname) {
        String fileId = this.backedUpFilesMap.remove(pathname);
        for (String chunkId : this.sentChunksMap.keySet()) {
            SentChunk chunk = this.sentChunksMap.get(chunkId);
            if (chunk.getFileId().equals(fileId)) {
                this.sentChunksMap.remove(chunkId);
            }
        }
        this.commit();
    }

    public Set<String> getDeletedFiles() {
        return deletedFiles;
    }

    public void fillBodyFromDisk(Chunk chunk) {
        if (chunk != null && chunk.getBody() == null) {
            String filepath = String.format(CHUNK_PATH, PEER_DIRECTORY, chunk.getFileId(), chunk.getChunkNo());
            File file = new File(filepath);
            try {
                chunk.setBody(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void forceFreeSpace() {
        ArrayList<SavedChunk> safeDeletions = new ArrayList<>();
        ArrayList<SavedChunk> unsafeDeletions = new ArrayList<>();
        for (String chunkId : this.getSavedChunksMap().keySet()) {
            SavedChunk chunk = this.getSavedChunksMap().get(chunkId);
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
        System.out.printf("[PEER] Occupation after safe deleting: %d\n", this.occupation);
        while (this.occupation > this.capacity && !unsafeDeletions.isEmpty()) {
            SavedChunk chunk = unsafeDeletions.remove(0);

            // System.out.printf("[PEER] Unsafe deleting %s\n", chunk.getChunkId());
            this.removeChunk(chunk);
        }
        System.out.printf("[PEER] Occupation after unsafe deleting: %d\n", this.occupation);
    }

    private void removeChunk(Chunk chunk) {
        this.deleteChunk(chunk);
        this.getSavedChunksMap().remove(chunk.getChunkId());
        this.commit();
        Message message = new RemovedMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), chunk.getFileId(), chunk.getChunkNo());
        this.peer.getMulticastControl().sendMessage(message);
    }

    public boolean freeSpace() {
        if (!this.acceptingRequests) return false;
        lockRequests(false);
        System.out.println("[PIS] Trying to free some space...");

        ArrayList<SavedChunk> safeDeletions = new ArrayList<>();
        for (String chunkId : this.getSavedChunksMap().keySet()) {
            SavedChunk chunk = this.getSavedChunksMap().get(chunkId);
            if (chunk.getPeers().size() > chunk.getReplicationDegree())
                safeDeletions.add(chunk);
        }

        while (!safeDeletions.isEmpty()) {
            SavedChunk chunk = safeDeletions.remove(0);

            System.out.printf("[PEER] Safe deleting %s\n", chunk.getChunkId());
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

    public void interruptPutchunks() {
        this.setAcceptingRequests(false);
    }

    private void setAcceptingRequests(boolean acceptingRequests) {
        this.acceptingRequests = acceptingRequests;
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

    private void lockRequests(boolean accepting) {
        this.acceptingRequests = accepting;
    }

    public boolean isAcceptingRequests() {
        return acceptingRequests;
    }

    public long calculateOccupation() {
        try {
            return directorySize(new File(PEER_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getOccupation() {
        return occupation;
    }

    public long getCapacity() {
        return capacity;
    }

    public String getPeerDirectory() {
        return PEER_DIRECTORY;
    }

    public static long size(Path path) {
        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    public long directorySize(File dir) throws IOException {
        Path folder = Paths.get(PEER_DIRECTORY);
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }
}
