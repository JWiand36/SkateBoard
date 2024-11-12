package sample;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sample.GUI.Display.AlertMessagePane;
import sample.GUI.Display.ClockPane;
import sample.GUI.Display.PromotionalMessagePane;
import sample.GUI.Display.RinkPane;
import sample.Model.Event;
import sample.Services.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

public class DisplayBoard {


    private int fontSize = 35; //35 for production.

    private double xOffset;
    private double yOffset;

    private final BorderPane mainBorder;
    private final BorderPane messageBorder;
    private final AlertMessagePane alertMessagePane;
    private final PromotionalMessagePane promotionalMessagePane;
    private final ClockPane clockPane;
    private final RinkPane rink1 = new RinkPane(fontSize, 1);
    private final RinkPane rink2 = new RinkPane(fontSize, 2);


    DisplayBoard() {
        messageBorder = new BorderPane();
        clockPane = new ClockPane(fontSize);
        alertMessagePane = new AlertMessagePane(fontSize);
        promotionalMessagePane = new PromotionalMessagePane(fontSize);
        mainBorder = new BorderPane();

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
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        messageBorder.setBottom(promotionalMessagePane);

        mainBorder.setBottom(messageBorder);
        mainBorder.setLeft(rink1);
        mainBorder.setRight(rink2);
        mainBorder.setTop(clockPane);
        mainBorder.setPadding(new Insets(5, 5, 0, 20));
    }

    void setScreenListeners(Stage primaryStage, Scene scene){
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

    BorderPane getMainBorder() { return mainBorder;}

    void setClock(String time){ clockPane.setClock(time); }

    void setAlertMessage(String message){ alertMessagePane.setAlertMessage(message); }

    void displayAlertMessage(boolean display){
        if(display)
            messageBorder.setTop(alertMessagePane);
        else
            messageBorder.setTop(null);
    }

    void setPromotionalMessage(String message){ promotionalMessagePane.setNewMessage(message); }

    void changeEvent(LocalDateTime currentTime, ArrayList<Event> rink1Events, ArrayList<Event> rink2Events){
        rink1.changeEvent(currentTime, rink1Events);
        rink2.changeEvent(currentTime, rink2Events);
    }

    void changeDay(LocalDateTime currentTime, ArrayList<Event> rink1Events, ArrayList<Event> rink2Events){
        displayNewDay(currentTime, rink1Events, rink2Events);
        Platform.runLater(()->messageBorder.setTop(null));
    }

    void updateBoard(LocalDateTime currentTime, ArrayList<Event> rink1Events, ArrayList<Event> rink2Events){ displayNewDay(currentTime, rink1Events, rink2Events);}

    private void displayNewDay(LocalDateTime currentTime, ArrayList<Event> rink1Events, ArrayList<Event> rink2Events){
        rink1.displayNewDay(currentTime, rink1Events);
        rink2.displayNewDay(currentTime, rink2Events);
    }

    void increaseTextSize(boolean increase, ArrayList<Event> rink1Events, ArrayList<Event> rink2Events) {

        if(increase){
            this.fontSize++;
        }else{
            this.fontSize--;
        }

        rink1.setFontSize(this.fontSize, rink1Events);
        rink2.setFontSize(this.fontSize, rink2Events);
        clockPane.setFontSize(this.fontSize);
        alertMessagePane.setFontSize(this.fontSize);
        promotionalMessagePane.setFontSize(this.fontSize);
    }
}
