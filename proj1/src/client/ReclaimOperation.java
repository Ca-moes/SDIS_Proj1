package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class ReclaimOperation extends Operation {
    private final long maxDiskSpace;

    public ReclaimOperation(InitiatorPeer stub, long maxDiskSpace) {
        super(stub);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.reclaim(this.maxDiskSpace);
    }
}
