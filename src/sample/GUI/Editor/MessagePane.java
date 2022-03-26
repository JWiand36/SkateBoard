package sample.GUI.Editor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sample.Controller;

//Sets up the MessagePane for both the Promotional message and the Alert message
public class MessagePane extends VBox {

    public MessagePane(int typeOfMessage, Controller controller){

        Text t = new Text();
        TextField tf = new TextField();
        Button submit = new Button("Submit");

        if(typeOfMessage == 1) {
            t.setText("The Promotional Message");

            submit.setOnAction(e -> {
                    controller.sendMessage(tf.getText());
            });
        }else {
            t.setText("The Alert Message");

            submit.setOnAction(e -> {
                    controller.sendAlertMessage(tf.getText());
            });
        }

        this.getChildren().addAll(t,tf,submit);
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.setAlignment(Pos.TOP_CENTER);
    }
}
