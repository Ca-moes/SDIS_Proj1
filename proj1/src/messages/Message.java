package messages;

import peer.Peer;
import tasks.Task;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Abstract Super Class Message
 */
public abstract class Message {
    protected final String protocolVersion;
    protected final String type;
    protected final int senderId;
    protected final String fileId;
    protected final int chunkNo;
    protected final int replicationDegree;
    protected final byte[] body;

    /**
     * Default Constructor for a Message
     *
     * @param protocolVersion   Current Protocol Version
     * @param type              Type of Message
     * @param senderId          Sender ID
     * @param fileId            File ID
     * @param chunkNo           Chunk Sequential Number
     * @param replicationDegree Desired Replication Degree
     * @param body              Chunk Body
     */
    public Message(String protocolVersion, String type, int senderId, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        this.protocolVersion = protocolVersion;
        this.type = type;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    /**
     * Create a Message from the DatagramPacket data
     *
     * @param packet       packed data in byte array
     * @param packetLength packed data's length
     * @return The parsed Message (taking advantage of Java's polymorphism)
     * @throws Exception On error trying to parse the packet
     */
    public static Message fromDatagramPacket(byte[] packet, int packetLength) throws Exception {
        String packetData = new String(packet);
        packetData = packetData.substring(0, Math.min(packetLength, packetData.length()));
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
        byte[] body = new byte[0];
        if (parts.length == 2) body = Arrays.copyOfRange(packet, headerBytes + 4, packetLength);

        String fileId;
        int chunkNo;
        int replicationDegree;

        switch (type) {
            case "CHUNK":
                fileId = args[3];
                chunkNo = Integer.parseInt(args[4]);
                return new ChunkMessage(version, senderId, fileId, chunkNo, body);
            case "DELETE":
                fileId = args[3];
                return new DeleteMessage(version, senderId, fileId);
            case "PUTCHUNK":
                fileId = args[3];
                chunkNo = Integer.parseInt(args[4]);
                replicationDegree = Integer.parseInt(args[5]);
                return new PutchunkMessage(version, senderId, fileId, chunkNo, replicationDegree, body);
            case "REMOVED":
                fileId = args[3];
                chunkNo = Integer.parseInt(args[4]);
                return new RemovedMessage(version, senderId, fileId, chunkNo);
            case "STORED":
                fileId = args[3];
                chunkNo = Integer.parseInt(args[4]);
                return new StoredMessage(version, senderId, fileId, chunkNo);
            case "GETCHUNK":
                fileId = args[3];
                chunkNo = Integer.parseInt(args[4]);
                return new GetchunkMessage(version, senderId, fileId, chunkNo);
            case "GENERALKENOBI":
                return new GeneralKenobi(version, senderId);
            default:
                throw new Exception("COULD NOT PARSE MESSAGE PACKET");
        }
    }

    /**
     * Method to encode the address and port to a byte array, this method is used for the
     * Restore Enhancement as we need to pass the address and port for the server socket on
     * the CHUNK message
     *
     * @param address Address to encode
     * @param port    Port to encode
     * @return The byte array containing the address and port in address:port format
     * @see jobs.SendChunk
     */
    public static byte[] addressPortToBytes(InetAddress address, int port) {
        return String.format("%s:%d", address.getHostAddress(), port).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @return True if the peer is enhanced
     */
    public boolean isEnhanced() {
        return !protocolVersion.equals("1.0");
    }

    /**
     * @return The encoded full header
     */
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

    /**
     * This method is used to discard own messages
     *
     * @param senderId Sender ID on the message
     * @return True if the sender ID passed as argument is the same as this message's sender ID
     */
    public boolean isOwner(int senderId) {
        return this.senderId == senderId;
    }

    //! Not documented
    @Override
    @Deprecated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return chunkNo == message.chunkNo && Objects.equals(protocolVersion, message.protocolVersion) && Objects.equals(type, message.type) && senderId == message.senderId && Objects.equals(fileId, message.fileId);
    }

    //! Not documented
    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, type, senderId, fileId, chunkNo);
    }

    //! Not documented
    @Override
    public String toString() {
        return "Message{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", type='" + type + '\'' +
                ", senderId='" + senderId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                '}';
    }

    /**
     * @param peer Peer used to get the associated worker
     * @return The related worker depending on the message type
     */
    abstract public ExecutorService getWorker(Peer peer);

    /**
     * @param peer Peer used to create the associated Task
     * @return The related Task depending on the message type
     */
    abstract public Task createTask(Peer peer);

    //! Not documented
    public String getProtocolVersion() {
        return protocolVersion;
    }

    //! Not documented
    public String getType() {
        return type;
    }

    //! Not documented
    public int getSenderId() {
        return senderId;
    }

    //! Not documented
    public String getFileId() {
        return fileId;
    }

    //! Not documented
    public int getChunkNo() {
        return chunkNo;
    }

    //! Not documented
    public int getReplicationDegree() {
        return replicationDegree;
    }

    //! Not documented
    public byte[] getBody() {
        return body;
    }
}
