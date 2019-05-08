package sample.client;

import sample.Event;
import sample.board.FileIO;

import java.io.IOException;
import java.util.ArrayList;

public class FileData {

    private ArrayList<Event> savedData = new ArrayList<>();

    FileData(){
        try {
            savedData = (ArrayList<Event>) FileIO.readFromFile("Data");
        } catch (IOException |ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    ArrayList<Event> getSavedData(){
        return savedData;
    }

    Event getEvent(int index){
        return savedData.get(index);
    }

    void removeEvent(int index){
        savedData.remove(index);
    }

    void saveData(){
        try {
            FileIO.writeToFile(savedData, "Data");
        }catch (IOException io){io.printStackTrace();}
    }
}
