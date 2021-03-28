package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * This class is responsible to start a State operation on the Initiator Peer
 *
 * @see InitiatorPeer
 * @see Operation
 */
public class StateOperation extends Operation {
    /**
     * State Operation Constructor
     *
     * @param stub Initiator Peer stub
     */
    public StateOperation(InitiatorPeer stub) {
        super(stub);
    }

    @Override
    public void start() throws RemoteException {
        System.out.println("Started a State Operation");
        System.out.println(this.stub.state());
    }
}
