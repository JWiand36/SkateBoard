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

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {


    private int fontSize = 35; //35 for production.

    private double xOffset;
    private double yOffset;

    private BorderPane messageBorder;
    private AlertMessagePane alertMessagePane;
    private PromotionalMessagePane promotionalMessagePane;
    private ClockPane clockPane;
    private RinkPane rink1 = new RinkPane(fontSize, 1);
    private RinkPane rink2 = new RinkPane(fontSize, 2);
    private Server server;
    private Clock clock;
    private EventCollection eventCollection;

    @Override
    public void start(Stage primaryStage) {

        messageBorder = new BorderPane();
        clockPane = new ClockPane(fontSize);
        alertMessagePane = new AlertMessagePane(fontSize);
        promotionalMessagePane = new PromotionalMessagePane(fontSize);
        BorderPane mainBorder = new BorderPane();

        eventCollection = new EventCollection(this);

        //Imports background photo from files
        try {
            mainBorder.setBackground(
                    new Background(
                            new BackgroundImage(
                                    FileIO.readImageFile("Ice", ".JPG"),
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundPosition.DEFAULT,
                                    new BackgroundSize(
                                            mainBorder.getWidth(),
                                            mainBorder.getHeight(),
                                            false,
                                            false,
                                            false,
                                            true)
                            )
                    )
            );
        }catch (IOException e){
            e.printStackTrace();
        }

        mainBorder.setBottom(promotionalMessagePane);
        mainBorder.setLeft(rink1);
        mainBorder.setRight(rink2);
        mainBorder.setTop(clockPane);
        mainBorder.setPadding(new Insets(5,5,0,20));

        new Thread(clock = new Clock(this)).start();
        new Thread(server = new Server(this)).start();

        //I don't like how this is solved, but this allows the program to wait until the clock is ready before
        // displaying the day
        while(!clock.isReady()){
            System.out.print("");
        }

        displayNewDay();

        Scene scene = new Scene(mainBorder, 1200, 650);
        setScreenListeners(mainBorder, primaryStage, scene);

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
            FileIO.writeToFile(eventCollection.getEvents(), "Events");
            FileIO.writeToFile(promotionalMessagePane.getText(), "Message");
        }catch (IOException io){io.getStackTrace();}
        clock.stop();
        server.stop();
        System.exit(0);
    }

    private void setScreenListeners(BorderPane mainBorder, Stage primaryStage, Scene scene){
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
    }

    void setClock(String time){
        clockPane.setClock(time);
    }

    void setAlertMessage(String message){
        alertMessagePane.setAlertMessage(message);
    }

    void displayAlertMessage(boolean display){
        if(display)
            messageBorder.setTop(alertMessagePane);
        else
            messageBorder.setTop(null);
    }

    void setPromotionalMessage(String message){
        promotionalMessagePane.setNewMessage(message);
    }

    void changeEvent(int hour, int min, String dayNightCycle, int week_day_number){
        rink1.changeEvent(hour, min, dayNightCycle, eventCollection.getDay(week_day_number));
        rink2.changeEvent(hour, min, dayNightCycle, eventCollection.getDay(week_day_number+7));
    }

    void changeDay(int week_day_number){
        displayNewDay();

        eventCollection.clearLastDay(week_day_number);

        Platform.runLater(()->messageBorder.setTop(null));
    }

    void displayNewDay(){
        rink1.displayNewDay(clock.getHour(), clock.getMin(), clock.dayNight(), eventCollection.getDay(clock.getDay()));
        rink2.displayNewDay(clock.getHour(), clock.getMin(), clock.dayNight(), eventCollection.getDay(clock.getDay()+7));
    }

    void setEvents(ArrayList<Event>[] events) { eventCollection.setEvents(events); }

    ArrayList<Event>[] getEvents(){
        return eventCollection.getEvents();
    }
}