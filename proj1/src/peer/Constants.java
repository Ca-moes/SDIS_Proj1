package peer;

public class Constants {
    public final static int CHUNK_SIZE = 64000;
    public final static long DEFAULT_CAPACITY = 100000000; // 100MB
    public final static int REQUESTS_WORKERS = 16;
    public final static int ACKS_WORKERS = 128;
    public final static int TRIAGE_WORKERS = 64;
    public final static int IO_WORKERS = 16;
}
