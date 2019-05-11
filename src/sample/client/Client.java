package sample.client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

import sample.Event;
import sample.board.FileIO;

/**
 * Created by John Wiand on 10/29/2016.
 */
public class Client extends Application {


    private EventCollection eventCollection;
    private NetworkService networkService;
    private FileData fileData = new FileData();
    private MainPane mainPane;
    private IPPane ipPane;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage){

        try {
            this.primaryStage = primaryStage;

            BorderPane pane;

            //The eventCollection is init here because it needs lists created which is done in MainPane
            mainPane = new MainPane(this);
            eventCollection = new EventCollection(this);
            networkService = new NetworkService(this);
            ipPane = new IPPane(this);

            pane = new BorderPane();
            pane.setTop(new MenuPane(this));
            pane.setCenter(mainPane);

            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(pane, 600, 650));

            try {
                networkService.setIPAddress((String) FileIO.readFromFile("IP"));
                networkService.inputData();
                primaryStage.show();
            } catch (IOException io) {
                io.printStackTrace();
                ipPane.showSecondStage();
            } catch (ClassNotFoundException not) {
                System.out.println("Error");
            }
        }catch (Exception e){e.printStackTrace();}
    }

    //Network Service Actions
    void setIPAddress(String ip){ networkService.setIPAddress(ip); }

    String getIPAddress(){ return networkService.getIPAddress(); }

    void sendMessage(String message){
        try {
            networkService.sendMessage(message);
        }catch (IOException io){showSecondWindow();}
    }

    void sendAlertMessage(String message){
        try{
            networkService.sendAlertMessage(message);
        }catch (IOException io){showSecondWindow();}
    }

    void removeAlertMessage(){
        try{
            networkService.removeAlertMessage();
        }catch (IOException io) {showSecondWindow();}
    }

    void inputData() {
        try{
            networkService.inputData();
        }catch (IOException io){showSecondWindow();}
        catch (ClassNotFoundException not) {not.printStackTrace(); }
    }

    void outputData() {
        try{
            networkService.outputData();
        }catch (IOException io) {showSecondWindow();}
    }

    //IP Pane Actions
    void showSecondWindow(){
        ipPane.showSecondStage();
    }

    //File Data Actions
    ArrayList<Event> getSavedData(){ return fileData.getSavedData(); }

    void removeSavedEvent(int index){ fileData.removeEvent(index); }

    Event getSavedEvent(int index) { return fileData.getEvent(index); }

    //Main Pane Actions
    ListView<String> getSavedInfo(){ return mainPane.getSavedInfo(); }

    void updateLists(ArrayList<Event>[] events){ mainPane.updateLists(events); }

    //Event Collection Actions
    void addEvent(int day, Event event){ eventCollection.addEvent(day, event); }

    Event getEvent(int day, int index){ return eventCollection.getEvent(day, index); }

    ArrayList<Event>[] getEvents(){ return eventCollection.getEvents(); }

    void removeEvent(int day, int index){ eventCollection.removeEvent(day, index);}

    void setCombinedEvents(ArrayList<Event>[] events){ this.eventCollection.setEvents(events); }

    ArrayList<Event>[] getSeparatedEvents() { return eventCollection.getSeparatedEvents(); }

    void combineEvents(ArrayList<Event>[] events){ eventCollection.combineEvents(events);}

    void resetCollection() { eventCollection.resetCollection(); }

    void sortEvents(boolean assign){ eventCollection.sortEvents(assign); }

    //Displays if the user makes a mistake
    void displayError(String message){
        Stage s = new Stage();

        Text t = new Text(message);
        t.setWrappingWidth(225);

        Button b = new Button("Ok");
        b.setOnAction(e->s.close());

        VBox p = new VBox(t, b);
        p.setAlignment(Pos.CENTER);
        p.setSpacing(10);

        s.setScene(new Scene(p, 250, 100));
        s.setTitle("Error Message!!");
        s.setResizable(false);
        s.show();
    }

    void showPrimary(){ primaryStage.show(); }

    @Override
    public void stop(){
        fileData.saveData();
        System.exit(0);
    }

}