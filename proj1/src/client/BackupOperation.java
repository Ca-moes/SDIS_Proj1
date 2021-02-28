package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class BackupOperation extends Operation {
    private final String pathname;
    private final int replicationDegree;

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
