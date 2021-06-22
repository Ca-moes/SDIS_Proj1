package messages;

import peer.Peer;
import tasks.ChunkTask;
import tasks.Task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CHUNK Message Data Class
 */
public class ChunkMessage extends Message {
    private InetAddress address;
    private int port;

    //! Not documented
    public ChunkMessage(String protocolVersion, int senderId, String fileId, int chunkNo, byte[] body) {
        super(protocolVersion, "CHUNK", senderId, fileId, chunkNo, 0, body);

        if (!protocolVersion.equals("1.0")) {
            Pattern p = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");
            Matcher m = p.matcher(new String(body));
            if (m.matches()) {
                try {
                    address = InetAddress.getByName(m.group(1));
                    port = Integer.parseInt(m.group(2));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //! Not documented
    public int getPort() {
        return port;
    }

    //! Not documented
    public InetAddress getAddress() {
        return address;
    }

    //! Not documented
    @Override
    public byte[] encodeToSend() {
        byte[] header = String.format("%s %s %s %s %d \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId,
                this.chunkNo).getBytes(StandardCharsets.UTF_8);

        byte[] toSend = new byte[header.length + this.body.length];
        System.arraycopy(header, 0, toSend, 0, header.length);
        System.arraycopy(this.body, 0, toSend, header.length, body.length);
        return toSend;
    }

    //! Not documented
    @Override
    public Task createTask(Peer peer) {
        return new ChunkTask(this, peer);
    }

    //! Not documented
    @Override
    public ExecutorService getWorker(Peer peer) {
        return peer.getAcknowledgmentsExecutor();
    }
}
