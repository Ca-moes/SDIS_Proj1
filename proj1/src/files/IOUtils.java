package files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is mostly Static, as the name suggests, this is a class for Utility methods
 */
public class IOUtils {
    /**
     * Method to create a file ID given a pathname
     *
     * @param pathname File's pathname to be created a file ID for
     * @return The file ID
     * @throws IOException On error reading the data
     */
    public static String getFileId(String pathname) throws IOException {
        File file = new File(pathname);

        BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
        String name = file.getName();
        String modificationDate = String.valueOf(attributes.lastModifiedTime().toMillis());

        return hashToASCII(name + modificationDate);
    }

    /**
     * Method to count how many chunks are on a file given a pathname using NIO
     *
     * @param pathname File's pathname to count the chunk number
     * @return This file's chunk count
     */
    public static int getNumberOfChunks(String pathname) {
        File file = new File(pathname);
        BasicFileAttributes attributes;
        try {
            attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();

            return (int) ((attributes.size() % 64000.0 == 0) ? attributes.size() / 64000.0 + 1 : Math.ceil(attributes.size() / 64000.0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Method to get the size of a file given a pathname using NIO
     *
     * @param pathname File's pathname to retrieve the file's size
     * @return The file's size in KB (1000B = 1KB)
     */
    public static double getSize(String pathname) {
        File file = new File(pathname);
        BasicFileAttributes attributes;
        try {
            attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
            return attributes.size() / 1000.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Method to hash a string and convert it to ASCII encoding
     *
     * @param string String to be hashed and converted
     * @return The Hash on ASCII encoding
     */
    public static String hashToASCII(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to convert a byte array to the Hexadecimal representation
     *
     * @param bytes Byte array to be converted to String on a Hexadecimal Representation
     * @return The byte array converted to a Hexadecimal String
     */
    private static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
