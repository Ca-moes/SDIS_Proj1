package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

public abstract class Operation {
    protected final InitiatorPeer stub;

    public Operation(InitiatorPeer stub) {
        this.stub = stub;
    }

    public static Operation createOperation(InitiatorPeer stub, String[] args) throws Exception {
        switch (args[1]) {
            case "BACKUP":
                return new BackupOperation(stub, args[2], Integer.parseInt(args[3]));
            case "RESTORE":
                return new RestoreOperation(stub, args[2]);
            case "DELETE":
                return new DeleteOperation(stub, args[2]);
            case "RECLAIM":
                return new ReclaimOperation(stub, Integer.parseInt(args[2]));
            case "DEBUG":
                return new DebugOperation(stub, args[2]);
            case "STATE":
                return new StateOperation(stub);
            default:
                throw new Exception("Cannot parse Operation");
        }
    }

    public abstract void start() throws RemoteException;
}
