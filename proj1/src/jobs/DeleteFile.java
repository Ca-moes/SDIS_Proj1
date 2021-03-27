package jobs;

import messages.DeleteMessage;
import messages.Message;
import peer.Peer;

import java.util.concurrent.TimeUnit;

/**
 * Job responsible for sending DELETE messages for a specified fileId
 */
public class DeleteFile implements Runnable {
    private final Peer peer;
    private final String fileId;
    private final String pathname;
    private final int timeout;

    /**
     * Constructor for this Job
     *
     * @param peer     Peer responsible for the Delete Job
     * @param fileId   File's ID to be deleted
     * @param pathname File's Pathname to be deleted
     * @param timeout  Timeout to start yet another delete job
     */
    public DeleteFile(Peer peer, String fileId, String pathname, int timeout) {
        this.peer = peer;
        this.fileId = fileId;
        this.pathname = pathname;
        this.timeout = timeout;
    }

    /**
     * Method to start this job, if the timeout is lesser than 5 it will send a DELETE message and start another job
     * to send yet another DELETE message (UDP unreliability) after a timeout. Otherwise it will remove the entry from
     * the map and add it to the delete files map (used on Delete Enhancement)
     *
     * @see messages.GeneralKenobi
     * @see tasks.GeneralKenobiTask
     */
    @Override
    public void run() {
        if (timeout <= 5) {
            Message message = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), fileId);
            this.peer.getMulticastControl().sendMessage(message);
            this.peer.getRequestsExecutor().schedule(new DeleteFile(peer, fileId, pathname, timeout * 2), timeout, TimeUnit.SECONDS);
        } else {
            System.out.println("[PEER] File Deleted");
            this.peer.getInternalState().deleteBackedUpEntries(pathname);
            this.peer.getInternalState().getDeletedFiles().add(fileId);
        }
    }
}
