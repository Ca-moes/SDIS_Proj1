package peer;

import files.*;
import jobs.BackupChunk;
import jobs.DeleteFile;
import messages.GeneralKenobi;
import messages.Message;
import messages.MulticastService;
import messages.RemovedMessage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class on this serverless system, everything related directly with a Peer
 * is listed here, the peer uses some functionalities from other auxiliary classes,
 * mainly its Internal State as it's managed by a class named PeerInternalState
 *
 * @see PeerInternalState
 */
public class Peer implements InitiatorPeer {
    private MulticastService multicastControl;
    private MulticastService multicastDataBackup;
    private MulticastService multicastDataRestore;

    private String serviceAccessPoint;
    private int peerId;
    private String protocolVersion;

    private final ExecutorService triageExecutor;
    private final ScheduledExecutorService requestsExecutor;
    private final ExecutorService acknowledgmentsExecutor;
    private final ExecutorService IOExecutor;

    private final InetAddress address;

    private final PeerInternalState internalState;

    /**
     * Main method, every peer starts here, the arguments are parsed, the database is either loaded or
     * created if it does not exist, and the 3 multicast channels are created and started
     *
     * @param args Arguments received in the command line at the invocation of the program
     * @throws Exception If anything fails starting the Peer
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <Protocol Version> <Peer ID> <Service Access Point> <MC> <MDB> <MDR>");
            System.out.println("MC MDB MDR: <IP> <PORT>");
            System.out.println("Service Access Point: RMI bind");
            return;
        }

        Peer peer = new Peer(args);
        try {
            InitiatorPeer stub = (InitiatorPeer) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peer.getServiceAccessPoint(), stub);
            System.err.println("[RMI] Registry Complete");
            System.err.println("[RMI] Service Access Point: " + peer.getServiceAccessPoint());
        } catch (Exception e) {
            System.err.println("[RMI] Registry Exception: " + e.getCause());
            System.out.println("[PEER] Will continue but RMI is offline");
        }

        peer.start();
    }

    /**
     * Main constructor for a peer: it takes the arguments passed in the command line, parses them and instantiates
     * the fields accordingly, here we also initialize the executors and schedulers. As mentioned on the report
     * we have 4 executors/schedulers for our concurrency model. For the Restore Enhancement we also define here the
     * IP address of this peer, and finally we load the database if it exists on the file system, otherwise a new database
     * is created from scratch
     *
     * @param args Arguments passed in the command line at the start
     * @throws IOException On a problem parsing the arguments or getting the localhost IP
     */
    public Peer(String[] args) throws IOException {
        parseArgs(args);

        this.triageExecutor = Executors.newFixedThreadPool(Constants.TRIAGE_WORKERS);
        this.requestsExecutor = Executors.newScheduledThreadPool(Constants.REQUESTS_WORKERS);
        this.acknowledgmentsExecutor = Executors.newFixedThreadPool(Constants.ACKS_WORKERS);
        this.IOExecutor = Executors.newFixedThreadPool(Constants.IO_WORKERS);

        this.address = InetAddress.getLocalHost();

        this.internalState = PeerInternalState.loadInternalState(this);
    }

    /**
     * Method to fire up the peer, it will start the channels on separate threads and will print some
     * useful messages on the default output stream, also, if this peer is enhanced it will send a
     * "General Kenobi" Message signaling other enhanced peers that this peer is now online. This enhancement
     * corresponds to the Delete Protocol. If this peer was online at the moment of a deletion this will come in handy.
     */
    private void start() {
        new Thread(this.multicastControl).start();
        new Thread(this.multicastDataBackup).start();
        new Thread(this.multicastDataRestore).start();

        System.out.printf("[PEER] Peer with ID:%d IS LIVE!\n", this.peerId);

        System.out.printf("[PEER] Saved Chunks: %d\n", this.internalState.getSavedChunksMap().size());
        System.out.printf("[PEER] Sent Chunks: %d\n", this.internalState.getSentChunksMap().size());
        System.out.printf("[PEER] Backed Up Files: %d\n", this.internalState.getBackedUpFilesMap().size());
        System.out.printf("[PEER] Occupation: %.2fKB\n", this.internalState.calculateOccupation() / 1000.0);
        System.out.printf("[PEER] Capacity: %.2fKB\n", this.internalState.getCapacity() / 1000.0);

        String version = this.protocolVersion + ((this.isEnhanced()) ? " - ENHANCED" : "");

        System.out.println("[PEER] Version: " + version);

        if (this.isEnhanced()) {
            System.out.println("[PEER] Cosplaying as General Kenobi and sending an 'Hello There' to peers listening...");
            this.multicastControl.sendMessage(new GeneralKenobi(this.protocolVersion, this.peerId));
        }
    }

    /**
     * Method to parse the arguments
     *
     * @param args Arguments passed in the command line
     * @throws IOException On error reading the IP addresses or creating a MulticastService
     * @see MulticastService
     */
    private void parseArgs(String[] args) throws IOException {
        this.protocolVersion = args[0];
        this.peerId = Integer.parseInt(args[1]);
        this.serviceAccessPoint = args[2];

        this.multicastControl = new MulticastService(InetAddress.getByName(args[3]), Integer.parseInt(args[4]), this, "MC");
        this.multicastDataBackup = new MulticastService(InetAddress.getByName(args[5]), Integer.parseInt(args[6]), this, "MDB");
        this.multicastDataRestore = new MulticastService(InetAddress.getByName(args[7]), Integer.parseInt(args[8]), this, "MDR");
    }

    /**
     * @return The multicast control channel
     */
    public MulticastService getMulticastControl() {
        return multicastControl;
    }

    /**
     * @return The multicast data backup channel
     */
    public MulticastService getMulticastDataBackup() {
        return multicastDataBackup;
    }

    /**
     * @return The multicast data restore channel
     */
    public MulticastService getMulticastDataRestore() {
        return multicastDataRestore;
    }

    /**
     * @return This peer's Service Access Point for RMI connections
     */
    public String getServiceAccessPoint() {
        return serviceAccessPoint;
    }

    /**
     * @return This peer's ID
     */
    public int getPeerId() {
        return peerId;
    }

    /**
     * @return This peer's Protocol Version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * @return This peer's Internal State
     */
    public PeerInternalState getInternalState() {
        return internalState;
    }

    /**
     * This method will start the backup procedure for a file with a given replication degree, to put this simple,
     * this method reads the file in chunks of 64KB (64000B) and for each it will start a BackupChunk job
     *
     * @see InitiatorPeer
     * @see BackupChunk
     */
    @Override
    public void backup(String pathname, int replicationDegree) throws RemoteException {
        System.out.println("[CLIENT] BACKUP PROTOCOL");

        int numberOfChunks = IOUtils.getNumberOfChunks(pathname);
        String original = pathname;

        try {
            BackedUpFile file = new BackedUpFile(pathname);
            System.out.printf("[CLIENT] Pathname: %s | Replication Degree: %d\nFile ID: %s\n", pathname, replicationDegree, file.getFileID());

            if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
                System.out.println("[BACKUP] There's already a backup for this pathname: " + pathname);
                if (this.internalState.getBackedUpFilesMap().get(pathname).getFileId().equals(file.getFileID())) {
                    System.out.println("[BACKUP] Delete this file before proceeding.");
                    return;
                }
                else {
                    File file1 = new File(pathname);
                    Path path = file1.toPath();

                    pathname = path.getParent() + "/dup_" + path.getFileName();

                    System.out.println("[BACKUP] New version of file, adding prefix, new filename: " + pathname);
                }
            }

            this.getInternalState().getBackedUpFilesMap().put(pathname, new ServerFile(original, file.getFileID(), replicationDegree, IOUtils.getSize(original)));
            this.getInternalState().commit();

            byte[] buffer;
            int i = 0;
            int size = 0;
            while ((buffer = file.getNextChunk()) != null) {
                size = buffer.length;
                SentChunk chunk = new SentChunk(file.getFileID(), i, replicationDegree);
                chunk.setBody(Arrays.copyOf(buffer, buffer.length));
                this.internalState.getSentChunksMap().put(chunk.getChunkId(), chunk);

                System.out.printf("[%s] SENDING CHUNK: %d of %d\n", pathname, i + 1, numberOfChunks);
                this.IOExecutor.submit(new BackupChunk(chunk, this, 1));
                i++;
            }
            if (size == 64000) {
                System.out.println("FILE WITH MULTIPLE OF 64KB, SENDING AN EMPTY BODY PUTCHAR MESSAGE");
                SentChunk chunk = new SentChunk(file.getFileID(), i, replicationDegree);
                chunk.setBody(new byte[0]);
                this.internalState.getSentChunksMap().put(chunk.getChunkId(), chunk);

                System.out.printf("[%s] SENDING CHUNK: %d of %d\n", pathname, i + 1, numberOfChunks);
                this.IOExecutor.submit(new BackupChunk(chunk, this, 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will start a restoration operation, put simply, it will create a FutureFile and
     * call the <code>restoreFile</code> method if the peer has that file backed up
     *
     * @see InitiatorPeer
     * @see FutureFile#restoreFile()
     */
    @Override
    public void restore(String pathname) throws RemoteException {
        System.out.println("[CLIENT] RESTORE PROTOCOL");
        System.out.printf("[CLIENT] Pathname: %s\n", pathname);

        if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
            System.out.println("[PEER] I backed up that file. Starting restoration...");
            String fileId = this.internalState.getBackedUpFilesMap().get(pathname).getFileId();

            FutureFile futureFile = new FutureFile(fileId, pathname, this);
            futureFile.restoreFile();
        } else {
            System.out.println("[PEER] I dont have that file backed up! Aborting...");
        }
    }

    /**
     * This method will trigger a delete operation, starting a DeleteFile job for a file ID if the pathname
     * is present on the backed up files map.
     *
     * @see PeerInternalState
     * @see DeleteFile
     */
    @Override
    public void delete(String pathname) throws RemoteException {
        System.out.println("[CLIENT] DELETE PROTOCOL");
        System.out.printf("[CLIENT] Pathname: %s\n", pathname);

        if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
            System.out.println("[PEER] I backed up that file. Starting deletion...");
            String fileId = this.internalState.getBackedUpFilesMap().get(pathname).getFileId();

            this.getIOExecutor().submit(new DeleteFile(this, fileId, pathname, 1));
        } else {
            System.out.println("[PEER] I have not backed up that file!");
        }
    }

    /**
     * This method will start a reclaim operation, if the maxDiskSpace is zero it will delete every single chunk and
     * set the default capacity defined in Constants, otherwise it will interrupt the PUTCHUNK tasks and prevent them from
     * starting for 60 seconds and delete chunks until it reaches the desired Maximum Space, starting with the chunks with
     * higher replication degree than the necessary
     *
     * @see PeerInternalState#forceFreeSpace()
     * @see Constants#DEFAULT_CAPACITY
     */
    @Override
    public void reclaim(long maxDiskSpace) throws RemoteException {
        System.out.println("[CLIENT] RECLAIM PROTOCOL");
        System.out.printf("[CLIENT] Reclaiming %d KB of storage - ('0' means all space)\n", maxDiskSpace);

        if (maxDiskSpace == 0) {
            this.internalState.interruptPutchunks();
            this.internalState.setCapacity(0);
            System.out.println("[PEER] Removing all chunks");
            // delete every chunk and reset capacity
            for (String chunkId : this.internalState.getSavedChunksMap().keySet()) {
                SavedChunk chunk = this.internalState.getSavedChunksMap().get(chunkId);
                this.internalState.deleteChunk(chunk);
                this.internalState.getSavedChunksMap().remove(chunkId);

                Message message = new RemovedMessage(this.protocolVersion, this.peerId, chunk.getFileId(), chunk.getChunkNo());
                this.multicastControl.sendMessage(message);
            }
            this.internalState.setCapacity(Constants.DEFAULT_CAPACITY);
            this.internalState.commit();
            return;
        }

        this.internalState.setCapacity(maxDiskSpace * 1000);
        if (maxDiskSpace <= this.internalState.getCapacity())
            this.internalState.interruptPutchunks();

        this.internalState.forceFreeSpace();
    }

    /**
     * This method will trigger a State Operation, we went a bit further on this than expected,
     * as reading the data on the command line can be a bit hard on the eyes we created a class
     * designed to assemble a webpage just to display the peer's state, made with love and bootstrap.
     * <p>
     * Of course, this "enhancement" changes nothing overall, we did it just for fun, the default
     * behaviour is still here, the requested information is still being printed to the standard output stream
     *
     * @see ReportMaker
     * @see PeerInternalState#toString()
     */
    @Override
    public String state() throws RemoteException {
        ReportMaker.toHTML(this.internalState);

        return this.internalState.toString();
    }

    /**
     * @return <code>true</code> if this peer is enhanced
     */
    public boolean isEnhanced() {
        return !protocolVersion.equals("1.0");
    }

    /**
     * @return This peer's address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @return The triage worker
     */
    public ExecutorService getTriageExecutor() {
        return triageExecutor;
    }

    /**
     * @return The Requests worker
     */
    public ScheduledExecutorService getRequestsExecutor() {
        return requestsExecutor;
    }

    /**
     * @return The Responses Worker (Acknowledgements)
     */
    public ExecutorService getAcknowledgmentsExecutor() {
        return acknowledgmentsExecutor;
    }

    /**
     * @return The IO worker
     */
    public ExecutorService getIOExecutor() {
        return IOExecutor;
    }
}
