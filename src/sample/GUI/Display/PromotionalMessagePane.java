package sample.GUI.Display;

import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sample.Services.FileIO;

import java.io.IOException;

public class PromotionalMessagePane extends Pane {


    private Text message;

    public PromotionalMessagePane(int fontSize){

        message = new Text("");
        message.setFont(new Font((fontSize)));

        this.getChildren().add(message);
        this.setMinHeight(40);

        try{
            message.setText((String) FileIO.readFromFile("Message"));
            runMessage();
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void setNewMessage(String message){
        this.message.setText(message);
        runMessage();
    }

    //Runs the promotional message displayed at the bottom of the screen
    private void runMessage(){
        int length = message.getText().length() * 10;
        PathTransition messagePath = new PathTransition();
        Line line = new Line(length+3500,25,-1400-length,25);

        messagePath.setDuration(Duration.millis(50000));
        messagePath.setCycleCount(Timeline.INDEFINITE);
        messagePath.setNode(message);
        messagePath.setPath(line);
        messagePath.play();
    }

    public String getText(){
        return message.getText();
    }

    public void setFontSize(int fontSize){ message.setFont(new Font(fontSize)); }
}
