package sample.board;

import javafx.scene.image.Image;

import java.io.*;

public class FileIO {

    public static void writeToFile(Object objectToFile, String nameOfFile) throws IOException {
        File f = new File(nameOfFile+".dat");
        FileOutputStream fs = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fs);

        os.writeObject(objectToFile);
        os.flush();
        os.close();
    }

    public static Object readFromFile(String nameOfFile) throws IOException, ClassNotFoundException{
        File f = new File(nameOfFile+".dat");
        FileInputStream fi = new FileInputStream(f);
        ObjectInputStream oi = new ObjectInputStream(fi);

        return oi.readObject();
    }

    //This pulls the background and logo images.
    public static Image readImageFile(String nameOfFile, String ext) throws IOException, ArrayIndexOutOfBoundsException{
        File f;
        try {
            f = new File(nameOfFile + ext);
            FileInputStream fi = new FileInputStream(f);
            return new Image(fi);
        }catch (Exception io){
            //I don't like this method, need to look in a better way to fix this.
            if(!ext.equals(".PNG"))
                return readImageFile(nameOfFile, ".PNG");
            else
                io.printStackTrace();
        }

        return new Image(nameOfFile + ".JPG");
    }

}
