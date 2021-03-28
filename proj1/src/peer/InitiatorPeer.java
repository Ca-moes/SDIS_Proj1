package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Stub interface for the Initiator Peer - Client Connection using RMI
 */
public interface InitiatorPeer extends Remote {
    /**
     * Start a backup operation for a file using its pathname
     *
     * @param pathname          File's pathname
     * @param replicationDegree Desired replication degree
     * @throws RemoteException On error connecting with RMI
     */
    void backup(String pathname, int replicationDegree) throws RemoteException;

    /**
     * Start a Restore Operation for a file using its pathname
     *
     * @param pathname File's Pathname
     * @throws RemoteException On error connecting with RMI
     */
    void restore(String pathname) throws RemoteException;

    /**
     * Start a Delete Operation for a file using its pathname
     *
     * @param pathname File's Pathname
     * @throws RemoteException On error connecting with RMI
     */
    void delete(String pathname) throws RemoteException;

    /**
     * Start a Reclaim Operation for the peer given the new maximum space allowed for Chunk backup
     *
     * @param maxDiskSpace Maximum allowed space for Chunk Backup
     * @throws RemoteException On error connecting with RMI
     */
    void reclaim(long maxDiskSpace) throws RemoteException;

    /**
     * Start a State Operation
     *
     * @return The state of the peer called on RMI
     * @throws RemoteException On error connecting with RMI
     */
    String state() throws RemoteException;
}
