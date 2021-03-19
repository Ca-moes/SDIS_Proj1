package files;

import peer.Constants;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class SentChunk extends Chunk implements Serializable {
    public SentChunk(String fileId, int chunkNo, int replicationDegree) {
        super(fileId, chunkNo, replicationDegree);
    }

    public SentChunk(String fileId, int chunkNo) {
        super(fileId, chunkNo);
    }

    private boolean connectionFailed = false;
    private boolean receivingData = false;

    @Override
    public int getReplicationDegree() {
        return replicationDegree;
    }


    public void loadBodyFromTCP(InetAddress address, int port) throws IOException {
        Socket client = new Socket(address.getHostAddress(), port);

        receivingData = true;

        DataInputStream stream = new DataInputStream(client.getInputStream());
        int readBytes = 0;
        int lastRead = 1;
        byte[] aux = new byte[Constants.CHUNK_SIZE];
        while (readBytes < Constants.CHUNK_SIZE && lastRead >= 0) {
            lastRead = stream.read(aux, readBytes, Constants.CHUNK_SIZE - readBytes);
            readBytes += Math.max(lastRead, 0);
        }
        this.body = Arrays.copyOf(aux, readBytes);
        receivingData = false;

        System.out.printf("[RESTORE] [TCP] Received %s : %d bytes\n", getChunkId(), body.length);
    }

    public void setConnectionFailed(boolean connectionFailed) {
        this.connectionFailed = connectionFailed;
    }

    public boolean connectionFailed() {
        return connectionFailed;
    }

    @Override
    public String toString() {
        return "\nSentChunk{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                ", replicationDegree=" + replicationDegree +
                ", peers=" + peers +
                '}';
    }

    public void setReceivingData(boolean receivingData) {
        this.receivingData = receivingData;
    }

    public boolean isReceivingData() {
        return receivingData;
    }
}
