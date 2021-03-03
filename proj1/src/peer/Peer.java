package peer;

import files.PeerFile;
import messages.DeleteMessage;
import messages.Message;
import messages.MulticastService;
import messages.PutchunkMessage;
import tasks.BackupChunk;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Peer implements InitiatorPeer {
    private MulticastService multicastControl;
    private MulticastService multicastDataBackup;
    private MulticastService multicastDataRestore;

    private String serviceAccessPoint;
    private String peerId;
    private String protocolVersion;

    private final ExecutorService threadPoolExecutor;

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
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(peer.getPeerId(), stub);
            System.err.println("[PEER] - RMI registry complete");
        } catch (Exception e) {
            System.err.println("[PEER] - RMI registry exception: " + e.toString());
            e.printStackTrace();
        }

        peer.start();
    }

    public Peer(String[] args) throws IOException {
        parseArgs(args);
        this.threadPoolExecutor = Executors.newFixedThreadPool(64);
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
        this.peerId = args[1];
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

    public Object getServiceAccessPoint() {
        return serviceAccessPoint;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public ExecutorService getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public PeerInternalState getInternalState() {
        return internalState;
    }

    @Override
    public void backup(String pathname, int replicationDegree) throws RemoteException {
        System.out.println("BACKUP PROTOCOL");
        System.out.printf("Pathname: %s | Replication Degree: %d\n", pathname, replicationDegree);

        try {
            PeerFile file = new PeerFile(pathname);
            this.getInternalState().getBackedUpFilesMap().put(pathname, file.getFileID());
            this.getInternalState().commit();

            byte[] buffer;
            int i = 1;
            int size = 0;
            while ((buffer = file.getNextChunk()) != null) {
                Message message = new PutchunkMessage(
                        this.protocolVersion,
                        this.peerId,
                        file.getFileID(),
                        i,
                        replicationDegree,
                        buffer
                );
                size = buffer.length;
                new Thread(new BackupChunk(message, this)).start();
                i++;
            }
            if (size == 64000) {
                System.out.println("FILE WITH MULTIPLE OF 64KB, SENDING AN EMPTY BODY PUTCHAR MESSAGE");
                Message message = new PutchunkMessage(
                        this.protocolVersion,
                        this.peerId,
                        file.getFileID(),
                        i,
                        replicationDegree,
                        new byte[0]
                );
                new Thread(new BackupChunk(message, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(String pathname) throws RemoteException {
        System.out.println("RESTORE PROTOCOL - Not yet implemented");
    }

    @Override
    public void delete(String pathname) throws RemoteException {
        System.out.println("DELETE PROTOCOL");
        System.out.printf("Pathname: %s\n", pathname);

        if (this.internalState.getBackedUpFilesMap().containsKey(pathname)) {
            System.out.println("[PEER] I backed up that file. Starting deletion...");
            String fileId = this.internalState.getBackedUpFilesMap().get(pathname);
            Message message = new DeleteMessage(this.protocolVersion, this.peerId, fileId);
            int timeout = 1000;
            while (timeout <= 5000) {
                try {
                    this.multicastControl.sendMessage(message);
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeout += 1000;
            }
            System.out.println("[PEER] File Deleted");
            this.getInternalState().deleteBackedUpEntries(pathname);

        } else {
            System.out.println("[PEER] I have not backed up that file!");
        }
    }

    @Override
    public void reclaim(int maxDiskSpace) throws RemoteException {
        System.out.println("RECLAIM PROTOCOL - Not yet implemented");
    }

    @Override
    public void state() throws RemoteException {
        System.out.println(this.internalState);
    }
}
