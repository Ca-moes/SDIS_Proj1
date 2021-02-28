package client;

import peer.InitiatorPeer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
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
