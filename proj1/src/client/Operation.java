package client;

import peer.InitiatorPeer;

import java.rmi.RemoteException;

/**
 * Abstract class responsible to start new operations on the Initiator Peer
 *
 * @see InitiatorPeer
 */
public abstract class Operation {
    protected final InitiatorPeer stub;

    /**
     * Super constructor
     *
     * @param stub Initiator Peer stub
     */
    public Operation(InitiatorPeer stub) {
        this.stub = stub;
    }

    /**
     * Static method to create an operation given a set of arguments and a stub
     *
     * @param stub Initiator Peer stub
     * @param args Arguments received on the command line at the start of the Client
     * @return A parsed operation to take advantage of polymorphism and OOP
     * @throws Exception On error parsing the arguments
     */
    public static Operation createOperation(InitiatorPeer stub, String[] args) throws Exception {
        switch (args[1]) {
            case "BACKUP":
                return new BackupOperation(stub, args[2], Integer.parseInt(args[3]));
            case "RESTORE":
                return new RestoreOperation(stub, args[2]);
            case "DELETE":
                return new DeleteOperation(stub, args[2]);
            case "RECLAIM":
                return new ReclaimOperation(stub, Long.parseLong(args[2]));
            case "STATE":
                return new StateOperation(stub);
            default:
                throw new Exception("Cannot parse Operation");
        }
    }

    /**
     * Abstract Method to start an operation, this method is of course implemented on the extended classes
     *
     * @throws RemoteException On error connecting with the Initiator Peer RMI service
     */
    public abstract void start() throws RemoteException;
}
