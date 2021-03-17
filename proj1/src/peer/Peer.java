package peer;

import files.*;
import jobs.DeleteFile;
import messages.*;
import jobs.BackupChunk;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final PeerInternalState internalState;

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.out.println("Usage: java Peer <MC> <MDB> <MDR> <Protocol Version> <Peer ID> <Service Access Point>");
            System.out.println("MC MDB MDR: <IP>");
            System.out.println("Service Access Point: ???");
            throw new Exception("Invalid Arguments Number");
        }

        Peer peer = new Peer(args);
        try {
            InitiatorPeer stub = (InitiatorPeer) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peer.getServiceAccessPoint(), stub);
            System.err.println("[PEER] - RMI registry complete");
        } catch (Exception e) {
            System.err.println("[PEER] - RMI registry exception: " + e.toString());
            e.printStackTrace();
        }

        peer.start();
    }

    public Peer(String[] args) throws IOException {
        parseArgs(args);

        this.triageExecutor = Executors.newFixedThreadPool(Constants.TRIAGE_WORKERS);
        this.requestsExecutor = Executors.newScheduledThreadPool(Constants.REQUESTS_WORKERS);
        this.acknowledgmentsExecutor = Executors.newFixedThreadPool(Constants.ACKS_WORKERS);
        this.IOExecutor = Executors.newFixedThreadPool(Constants.IO_WORKERS);

        this.internalState = PeerInternalState.loadInternalState(this);
    }

    private void start() {
        new Thread(this.multicastControl).start();
        new Thread(this.multicastDataBackup).start();
        new Thread(this.multicastDataRestore).start();

        System.out.printf("PEER %s IS LIVE!\n", this.peerId);
    }

    private void parseArgs(String[] args) throws IOException {
        this.protocolVersion = args[0];
        this.peerId = Integer.parseInt(args[1]);
        this.serviceAccessPoint = args[2];
        // parse multicast control
        Pattern p = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");
        Matcher m = p.matcher(args[3]);
        if (m.matches()) {
            InetAddress host = InetAddress.getByName(m.group(1));
            int port = Integer.parseInt(m.group(2));
            this.multicastControl = new MulticastService(host, port, this, "MC");
        }
        // parse multicast data backup
        m = p.matcher(args[4]);
        if (m.matches()) {
            InetAddress host = InetAddress.getByName(m.group(1));
            int port = Integer.parseInt(m.group(2));
            this.multicastDataBackup = new MulticastService(host, port, this, "MDB");
        }
        // parse multicast data restore
        m = p.matcher(args[5]);
        if (m.matches()) {
            InetAddress host = InetAddress.getByName(m.group(1));
            int port = Integer.parseInt(m.group(2));
            this.multicastDataRestore = new MulticastService(host, port, this, "MDR");
        }
    }

    public MulticastService getMulticastControl() {
        return multicastControl;
    }

    public MulticastService getMulticastDataBackup() {
        return multicastDataBackup;
    }

    public MulticastService getMulticastDataRestore() {
        return multicastDataRestore;
    }

    public String getServiceAccessPoint() {
        return serviceAccessPoint;
    }

    public int getPeerId() {
        return peerId;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public PeerInternalState getInternalState() {
        return internalState;
    }

    @Override
    public void backup(String pathname, int replicationDegree) throws RemoteException {
        System.out.println("BACKUP PROTOCOL");
        System.out.printf("Pathname: %s | Replication Degree: %d\n", pathname, replicationDegree);

        int numberOfChunks = IOUtils.getNumberOfChunks(pathname);

        try {
            BackedUpFile file = new BackedUpFile(pathname);
            this.getInternalState().getBackedUpFilesMap().put(pathname, file.getFileID());
            this.getInternalState().commit();

            byte[] buffer;
            int i = 0;
            int size = 0;
            while ((buffer = file.getNextChunk()) != null) {
                size = buffer.length;
                SentChunk chunk = new SentChunk(file.getFileID(), i, replicationDegree);
                chunk.setBody(Arrays.copyOf(buffer, buffer.length));
                this.internalState.getSentChunksMap().put(chunk.getChunkId(), chunk);

                System.out.printf("[%s] SENDING CHUNK: %d of %d\n", pathname, i+1, numberOfChunks);
                this.IOExecutor.submit(new BackupChunk(chunk, this, 1));
                i++;
            }
            if (size == 64000) {
                System.out.println("FILE WITH MULTIPLE OF 64KB, SENDING AN EMPTY BODY PUTCHAR MESSAGE");
                SentChunk chunk = new SentChunk(file.getFileID(), i, replicationDegree);
                chunk.setBody(Arrays.copyOf(buffer, buffer.length));
                this.internalState.getSentChunksMap().put(chunk.getChunkId(), chunk);

                System.out.printf("[%s] SENDING CHUNK: %d of %d\n", pathname, i+1, numberOfChunks);
                this.IOExecutor.submit(new BackupChunk(chunk, this, 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(String pathname) throws RemoteException {
        System.out.println("RESTORE PROTOCOL");
        System.out.printf("Pathname: %s\n", pathname);

        if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
            System.out.println("[PEER] I backed up that file. Starting restoration...");
            String fileId = this.internalState.getBackedUpFilesMap().get(pathname);

            FutureFile futureFile = new FutureFile(fileId, pathname, this);
            futureFile.restoreFile();
        } else {
            System.out.println("[PEER] I dont have that file backed up! Aborting...");
        }
    }

    @Override
    public void delete(String pathname) throws RemoteException {
        System.out.println("DELETE PROTOCOL");
        System.out.printf("Pathname: %s\n", pathname);

        if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
            System.out.println("[PEER] I backed up that file. Starting deletion...");
            String fileId = this.internalState.getBackedUpFilesMap().get(pathname);

            this.getIOExecutor().submit(new DeleteFile(this, fileId, pathname, 1));
        } else {
            System.out.println("[PEER] I have not backed up that file!");
        }
    }

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
            this.internalState.commit();
            return;
        }

        this.internalState.setCapacity(maxDiskSpace*1000);
        if (maxDiskSpace <= this.internalState.getCapacity())
            this.internalState.interruptPutchunks();

        this.internalState.forceFreeSpace();
    }

    @Override
    public String state() throws RemoteException {
        return this.internalState.toString();
    }

    public ExecutorService getTriageExecutor() {
        return triageExecutor;
    }

    public ScheduledExecutorService getRequestsExecutor() {
        return requestsExecutor;
    }

    public ExecutorService getAcknowledgmentsExecutor() {
        return acknowledgmentsExecutor;
    }

    public ExecutorService getIOExecutor() {
        return IOExecutor;
    }
}
