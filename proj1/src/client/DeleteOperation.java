package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * This class is responsible to start a Delete operation on the Initiator Peer
 *
 * @see InitiatorPeer
 * @see Operation
 */
public class DeleteOperation extends Operation {
    private final String pathname;

    /**
     * Delete Operation Constructor
     *
     * @param stub     Initiator Peer stub
     * @param pathname File path to be deleted
     */
    public DeleteOperation(InitiatorPeer stub, String pathname) {
        super(stub);
        this.pathname = pathname;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.delete(this.pathname);
    }
}
