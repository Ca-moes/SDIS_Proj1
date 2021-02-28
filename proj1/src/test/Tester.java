package test;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import files.SavedChunk;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void main(String[] args) {
        SavedChunk chunk = new SavedChunk("chunk1", 4, 3);

        List<SavedChunk> chunkList = new ArrayList<>();
        for (int i = 0; i < 100000; i++)
            chunkList.add(chunk);

        try
        {
            FileOutputStream fos = new FileOutputStream("chunks.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(chunkList);
            oos.close();
            fos.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }


        ArrayList<SavedChunk> chunks;

        long start = System.currentTimeMillis();

        try
        {
            FileInputStream fis = new FileInputStream("chunks.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);

            chunks = (ArrayList<SavedChunk>) ois.readObject();

            ois.close();
            fis.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }
        catch (ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

        long elapsedTime = System.currentTimeMillis() - start;

        System.out.println(elapsedTime);

        for (SavedChunk c : chunks) {
            System.out.println(c);
        }
    }
}
