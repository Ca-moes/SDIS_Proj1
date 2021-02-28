package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public class DebugOperation extends Operation {
    private final String message;

    public DebugOperation(InitiatorPeer stub, String message) {
        super(stub);
        this.message = message;
    }

    @Override
    public void start() throws RemoteException {
        System.out.println("Sent Debug Message - " + this.message);
        this.stub.debug(this.message);
    }
}
