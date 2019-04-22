package sample.board;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;

public class ClockPane extends FlowPane{

    private Text clockText;
    private ImageView logo;

    ClockPane(int fontSize){

        clockText = new Text();
        clockText.setFont(new Font(fontSize));

        //Imports photos from files
        try {
            logo = new ImageView(FileIO.readImageFile("logo", ".JPG"));
        }catch (IOException e){
            e.printStackTrace();
        }

        logo.setFitHeight(125);
        logo.setFitWidth(275);

        this.getChildren().addAll(logo,clockText);
        this.setAlignment(Pos.CENTER);
    }

    void setClock(String time){
        clockText.setText(time);
    }
}
