package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class RestoreOperation extends Operation {
    private final String pathname;

    public RestoreOperation(InitiatorPeer stub, String pathname) {
        super(stub);
        this.pathname = pathname;
    }

    @Override
    public void start() throws RemoteException {
        this.stub.restore(this.pathname);
    }
}
