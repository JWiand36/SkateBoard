package sample.board;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

class AlertMessagePane extends FlowPane{


    private Text specialMessage;

    AlertMessagePane(int fontSize){
        specialMessage = new Text("");
        specialMessage.setFont(new Font(fontSize));

        specialMessage.setStyle("-fx-font-weight: bold; " +
                "-fx-fill: white;");


        this.setStyle("-fx-background-color: #cc0000;" +
                "-fx-alignment: top-center;" +
                "-fx-pref-height: 40");
        this.getChildren().add(specialMessage);
    }


    void setAlertMessage(String text){
        specialMessage.setText(text);
    }
}
