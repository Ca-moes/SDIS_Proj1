package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * This class is responsible to start a backup operation on the Initiator Peer
 *
 * @see InitiatorPeer
 * @see Operation
 */
public class BackupOperation extends Operation {
    private final String pathname;
    private final int replicationDegree;

    /**
     * Backup Operation Constructor
     *
     * @param stub              Initiator Peer stub
     * @param pathname          File path to be backed up
     * @param replicationDegree Desired replication degree
     */
    public BackupOperation(InitiatorPeer stub, String pathname, int replicationDegree) {
        super(stub);
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void start() throws RemoteException {
        System.out.println("Started a Backup Operation");
        this.stub.backup(this.pathname, this.replicationDegree);
    }
}
