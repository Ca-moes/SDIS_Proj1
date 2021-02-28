package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class ReclaimOperation extends Operation {
    private final int maxDiskSpace;

    public ReclaimOperation(InitiatorPeer stub, int maxDiskSpace) {
        super(stub);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.reclaim(this.maxDiskSpace);
    }
}
