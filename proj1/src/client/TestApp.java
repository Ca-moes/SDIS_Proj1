package client;

import peer.InitiatorPeer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client's Interface to request operations on the serverless system
 */
public class TestApp {
    /**
     * <h1>Main Method on this TestApp</h1>
     * <p>
     * This method tries to connect to the RMI access point of a given initiator peer,
     * after that it will try to create an operation given the arguments received,
     * and finally it will initiate an operation.</p>
     *
     * @param args Arguments received on the command line at the start
     * @see Operation
     * @see InitiatorPeer
     */
    public static void main(String[] args) {
        String peerId = args[0];
        InitiatorPeer stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            stub = (InitiatorPeer) registry.lookup(peerId);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        try {
            Operation operation = Operation.createOperation(stub, args);
            operation.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
