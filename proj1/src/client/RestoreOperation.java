package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * This class is responsible to start a Restore operation on the Initiator Peer
 *
 * @see InitiatorPeer
 * @see Operation
 */
public class RestoreOperation extends Operation {
    private final String pathname;

    /**
     * Restore Operation Constructor
     *
     * @param stub     Initiator Peer stub
     * @param pathname Pathname for the file to be restored
     */
    public RestoreOperation(InitiatorPeer stub, String pathname) {
        super(stub);
        this.pathname = pathname;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.restore(this.pathname);
    }
}
