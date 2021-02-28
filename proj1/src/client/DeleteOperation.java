package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class DeleteOperation extends Operation {
    private final String pathname;

    public DeleteOperation(InitiatorPeer stub, String pathname) {
        super(stub);
        this.pathname = pathname;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.delete(this.pathname);
    }
}
