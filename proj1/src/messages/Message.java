package messages;

import peer.Peer;
import tasks.Task;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public abstract class Message {
    protected final String protocolVersion;
    protected final String type;
    protected final int senderId;
    protected final String fileId;
    protected final int chunkNo;
    protected final int replicationDegree;
    protected final byte[] body;

    public Message(String protocolVersion, String type, int senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        this.protocolVersion = protocolVersion;
        this.type = type;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    public static Message fromDatagramPacket(DatagramPacket packet) throws Exception {
        String packetData = new String(packet.getData());
        packetData = packetData.substring(0, Math.min(packet.getLength(), packetData.length()));
        // Splitting the packet data into two parts -> header and body, the splitter is two CRLF i.e. 2 \r\n
        String[] parts = packetData.split("\r\n\r\n", 2);

        int headerBytes = parts[0].length();
        // as the header may have two or more spaces between the arguments, then this replace statement cleans the header
        // we also want to trim the result so no trailing or leading spaces are present
        parts[0] = parts[0].replaceAll("^ +| +$|( )+", "$1").trim();

        // the arguments are split by a space
        String[] args = parts[0].split(" ");

        // saving the data
        String version = args[0];
        String type = args[1];
        int senderId = Integer.parseInt(args[2]);
        String fileId = args[3];
        byte[] body = new byte[0];
        if (parts.length == 2) body = Arrays.copyOfRange(packet.getData(), headerBytes + 4, packet.getLength());

        int chunkNo;
        int replicationDegree;

        switch (type) {
            case "CHUNK":
                chunkNo = Integer.parseInt(args[4]);
                return new ChunkMessage(version, senderId, fileId, chunkNo, body);
            case "DELETE":
                return new DeleteMessage(version, senderId, fileId);
            case "PUTCHUNK":
                chunkNo = Integer.parseInt(args[4]);
                replicationDegree = Integer.parseInt(args[5]);
                return new PutchunkMessage(version, senderId, fileId, chunkNo, replicationDegree, body);
            case "REMOVED":
                chunkNo = Integer.parseInt(args[4]);
                return new RemovedMessage(version, senderId, fileId, chunkNo);
            case "STORED":
                chunkNo = Integer.parseInt(args[4]);
                return new StoredMessage(version, senderId, fileId, chunkNo);
            default:
                throw new Exception("COULD NOT PARSE MESSAGE PACKET");
        }
    }

    public byte[] encodeToSend() {
        return String.format("%s %s %d %s %d %d \r\n\r\n",
                this.protocolVersion,
                this.type,
                this.senderId,
                this.fileId,
                this.chunkNo,
                this.replicationDegree)
                .getBytes(StandardCharsets.UTF_8);
    }

    public boolean isOwner(int senderId) {
        return this.senderId == senderId;
    }

    @Override
    @Deprecated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return chunkNo == message.chunkNo && Objects.equals(protocolVersion, message.protocolVersion) && Objects.equals(type, message.type) && senderId == message.senderId && Objects.equals(fileId, message.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, type, senderId, fileId, chunkNo);
    }

    @Override
    public String toString() {
        return "Message{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", type='" + type + '\'' +
                ", senderId='" + senderId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                /*", body=" + Arrays.toString(body) +*/
                '}';
    }

    abstract public Task createTask(Peer peer);

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public byte[] getBody() {
        return body;
    }
}
