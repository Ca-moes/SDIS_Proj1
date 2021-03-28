package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * This class is responsible to start a Reclaim operation on the Initiator Peer
 *
 * @see InitiatorPeer
 * @see Operation
 */
public class ReclaimOperation extends Operation {
    private final long maxDiskSpace;

    /**
     * Reclaim Operation Constructor
     *
     * @param stub         Initiator Peer stub
     * @param maxDiskSpace New max disk space to be used to backup chunks
     */
    public ReclaimOperation(InitiatorPeer stub, long maxDiskSpace) {
        super(stub);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.reclaim(this.maxDiskSpace);
    }
}
