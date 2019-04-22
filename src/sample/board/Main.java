package sample.board;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sample.Event;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.ArrayList;

public class Main extends Application {


    private int fontSize = 35; //35 for production.

    private double xOffset;
    private double yOffset;

    private ArrayList<sample.Event>[] events = new ArrayList[14];
    private BorderPane innerBorder;
    private AlertMessagePane alertMessagePane;
    private PromotionalMessagePane promotionalMessagePane;
    private ClockPane clockPane;
    private RinkPane rink1 = new RinkPane(fontSize, 1);
    private RinkPane rink2 = new RinkPane(fontSize, 2);
    private Server server;
    private Clock clock;

    @Override
    public void start(Stage primaryStage) {

        innerBorder = new BorderPane();
        clockPane = new ClockPane(fontSize);
        alertMessagePane = new AlertMessagePane(fontSize);
        promotionalMessagePane = new PromotionalMessagePane(fontSize);
        BorderPane mainBorder = new BorderPane();

        for (int i = 0; i < events.length; i++)
            events[i] = new ArrayList<>();

        //Imports the events from file
        try{
            events = (ArrayList<sample.Event>[]) FileIO.readFromFile("Events");
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        //Imports photos from files
        try {
            mainBorder.setBackground(new Background(new BackgroundImage(FileIO.readImageFile("Ice", ".JPG"),
                    BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,
                    new BackgroundSize(mainBorder.getWidth(),mainBorder.getHeight(),false,false,false,true))));
        }catch (IOException e){
            e.printStackTrace();
        }

        innerBorder.setBottom(promotionalMessagePane);

        mainBorder.setBottom(innerBorder);
        mainBorder.setLeft(rink1);
        mainBorder.setRight(rink2);
        mainBorder.setTop(clockPane);
        mainBorder.setPadding(new Insets(5,5,0,20));

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Scene scene = new Scene(mainBorder, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());

        new Thread(clock = new Clock(this)).start();
        new Thread(server = new Server(this)).start();

        //The next to listeners allow the user to click anywheres and drag the board.
        //grab the main border
        mainBorder.setOnMousePressed(e-> {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
        });

        //move around here
        mainBorder.setOnMouseDragged(e -> {
                primaryStage.setX(e.getScreenX() - xOffset);
                primaryStage.setY(e.getScreenY() - yOffset);
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, e-> {
            if(e.getCode()== KeyCode.SPACE) {
                primaryStage.setFullScreen(true);
            }

            if(e.getCode() == KeyCode.BACK_SPACE){
                primaryStage.close();
            }
        });

        //I don't like how this is solved, but this allows the program to wait until the clock is ready before
        // displaying the day
        while(!clock.isReady()){
            System.out.print("");
        }

        displayNewDay();

        primaryStage.setTitle("");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop(){
        try {
            FileIO.writeToFile(events, "Events");
            FileIO.writeToFile(promotionalMessagePane.getText(), "Message");
        }catch (IOException io){io.getStackTrace();}
        clock.stop();
        server.stop();
        System.exit(0);
    }

    private void displayNewDay(){
        rink1.displayNewDay(clock.getHour(), clock.getMin(), clock.dayNight(), events[clock.getDay()]);
        rink2.displayNewDay(clock.getHour(), clock.getMin(), clock.dayNight(), events[clock.getDay()+7]);
    }

    //Sorts the arrays of Events to be in ascending order of time, it uses the merge sorting technique.
    void sortEvents(ArrayList<sample.Event>[] events){
        System.out.println("Sorting");
        ArrayList<sample.Event>[] result = new ArrayList[events.length];
        ArrayList<sample.Event>[] sortArrays = new ArrayList[12];
        ArrayList<sample.Event> am = new ArrayList<>();
        ArrayList<sample.Event> pm = new ArrayList<>();

        //sets up the result and the array used to sort the data
        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < sortArrays.length; i++)
            sortArrays[i] = new ArrayList<>();

        for(int i = 0; i < result.length; i++){

            //Splits the data from Day/Night Cycle, AM will be completed first
            for(int k = 0; k < events[i].size(); k++){
                if(events[i].get(k).getDayNightCycle().equals("am"))
                    am.add(events[i].get(k));
                else
                    pm.add(events[i].get(k));

            }

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < am.size(); k++)
                sortArrays[am.get(k).getStartHour()%12].add(am.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int index = 0;
                while(sortArrays[k].size() > 1){
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(index).getStartMin() > sortArrays[k].get(c).getStartMin()){
                            index = sortArrays[k].indexOf(sortArrays[k].get(c));
                        }
                    }
                    result[i].add(sortArrays[k].get(index));
                    sortArrays[k].remove(index);
                    index = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            am.clear();

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < pm.size(); k++)
                sortArrays[pm.get(k).getStartHour()%12].add(pm.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int index = 0;
                while(sortArrays[k].size() > 1){
                    System.out.println("Size: " + sortArrays[k].size()+", Index: "+k);
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(index).getStartMin() > sortArrays[k].get(c).getStartMin()){
                            index = sortArrays[k].indexOf(sortArrays[k].get(c));
                        }
                    }
                    result[i].add(sortArrays[k].get(index));
                    sortArrays[k].remove(index);
                    index = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            pm.clear();
        }
        this.events = result;

        displayNewDay();
    }

    void changeDay(int week_day_number){
        displayNewDay();
            if (week_day_number != 0) {
                events[week_day_number - 1].clear();
                events[week_day_number + 6].clear();
            } else {
                events[week_day_number + 6].clear();
                events[week_day_number + 13].clear();
            }

        Platform.runLater(()->innerBorder.setTop(null));
    }

    void setClock(String time){
        clockPane.setClock(time);
    }

    void setAlertMessage(String message){
        alertMessagePane.setAlertMessage(message);
    }

    void displayAlertMessage(boolean display){
        if(display)
            innerBorder.setTop(alertMessagePane);
        else
            innerBorder.setTop(null);
    }

    void setPromotionalMessage(String message){
        promotionalMessagePane.setNewMessage(message);
    }

    void changeEvent(int hour, int min, String dayNightCycle, int week_day_number){
        rink1.changeEvent(hour, min, dayNightCycle, events[week_day_number]);
        rink2.changeEvent(hour, min, dayNightCycle, events[week_day_number+7]);
    }

    ArrayList<Event>[] getEvents(){
        return events;
    }
}