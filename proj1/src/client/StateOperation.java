package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class StateOperation extends Operation {
    public StateOperation(InitiatorPeer stub) {
        super(stub);
    }

    @Override
    public void start() throws RemoteException {
        System.out.println("Started a State Operation");
        this.stub.state();
    }
}
