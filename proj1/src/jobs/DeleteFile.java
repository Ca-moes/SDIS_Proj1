package jobs;

import messages.DeleteMessage;
import messages.Message;
import peer.Peer;

import java.util.concurrent.TimeUnit;

public class DeleteFile implements Runnable {
    private final Peer peer;
    private final String fileId;
    private final String pathname;
    private final int timeout;

    public DeleteFile(Peer peer, String fileId, String pathname, int timeout) {
        this.peer = peer;
        this.fileId = fileId;
        this.pathname = pathname;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        if (timeout <= 5) {
            Message message = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getPeerId(), fileId);
            this.peer.getMulticastControl().sendMessage(message);
            this.peer.getRequestsExecutor().schedule(new DeleteFile(peer, fileId, pathname, timeout*2), timeout, TimeUnit.SECONDS);
        } else {
            System.out.println("[PEER] File Deleted");
            this.peer.getInternalState().deleteBackedUpEntries(pathname);
            this.peer.getInternalState().getDeletedFiles().add(fileId);
        }
    }
}
