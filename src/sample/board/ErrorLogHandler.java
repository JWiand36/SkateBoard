package sample.board;

import java.io.*;
import java.util.ArrayList;

public class ErrorLogHandler {

    void processError(String errorMessage){

        File errorFile = new File("Log.dat");
        ArrayList<String> errors;

        //Inputs the file, if it can't be found a new ArrayList is created
        try{
            FileInputStream fi = new FileInputStream(errorFile);
            ObjectInputStream is = new ObjectInputStream(fi);
            errors = (ArrayList<String>)is.readObject();
        }catch (IOException | ClassNotFoundException e){
            errors = new ArrayList<>();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errors.add(sw.toString());
        }

        //Add the error to the list of errors;
        if(!errors.contains(errorMessage))
            errors.add(errorMessage);

        //Saves the ArrayList to the file "Log.dat"
        try {
            FileOutputStream fo = new FileOutputStream(errorFile);
            ObjectOutputStream os = new ObjectOutputStream(fo);

            os.writeObject(errors);
            os.flush();
            os.close();
        }catch (IOException io){ io.printStackTrace();}
    }

}
