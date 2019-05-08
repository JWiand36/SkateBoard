package sample.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

//Sets up the MessagePane for both the Promotional message and the Alert message
class MessagePane extends VBox {

    MessagePane(int typeOfMessage, Client client){

        Stage thirdStage = new Stage();

        Text t = new Text();
        TextField tf = new TextField();
        Button submit = new Button("Submit");

        if(typeOfMessage == 1) {
            t.setText("The Promotional Message");

            submit.setOnAction(e -> {
                    client.sendMessage(tf.getText());
                    thirdStage.close();
            });
        }else {
            t.setText("The Alert Message");

            submit.setOnAction(e -> {
                    client.sendAlertMessage(tf.getText());
                    thirdStage.close();
            });
        }

        this.getChildren().addAll(t,tf,submit);
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.setAlignment(Pos.TOP_CENTER);

        Platform.runLater(()->{
            thirdStage.setScene(new Scene(this, 250, 100));
            thirdStage.setTitle("Message");
            thirdStage.setResizable(false);
            thirdStage.show();
        });

    }
}
