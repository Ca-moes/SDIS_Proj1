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

public class IOUtils {
    public static String getFileId(String pathname) throws IOException {
        File file = new File(pathname);

        BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
        String name = file.getName();
        String modificationDate = String.valueOf(attributes.creationTime().toMillis());

        byte[] myBuffer = new byte[64000];
        InputStream in = new FileInputStream(pathname);

        String dataSHA = "";

        while ((in.read(myBuffer,0,64000) != -1)) {
            String hex = bytesToHex(myBuffer);
            dataSHA = hashToASCII(dataSHA + hex);
        }

        return hashToASCII(name + modificationDate + dataSHA);
    }

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
