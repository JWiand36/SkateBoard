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
import sample.board.FileIO;

import java.io.IOException;

//Displays the window asking for the Servers IP Address
class IPPane extends VBox {

    private Stage secondaryStage = new Stage();

    IPPane(Stage primaryStage, NetworkService networkService){


        Text incorrect = new Text();
        Text ipText = new Text("What is the Ip Address of the Server?");
        TextField ipTF = new TextField();
        Button connect = new Button("Connect");

        this.getChildren().addAll(incorrect,ipText,ipTF,connect);
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.TOP_CENTER);

        incorrect.setStyle("-fx-fill: Red;");

        //Once the connect button is pressed, the Program will collect data from the Server and save the IP address
        //giving to a file. If an IP error occurs the IP Address will continue to display and provide an error message
        connect.setOnAction(e->{
            networkService.setIPAddress(ipTF.getText());

            new Thread(()-> {
                try {
                    networkService.inputData();
                    Platform.runLater(()->{
                        secondaryStage.close();
                        primaryStage.show();
                        incorrect.setText("");
                        try {
                            FileIO.writeToFile(networkService.getIPAddress(), "IP");
                        }catch (IOException io){io.printStackTrace();System.out.println("Error Writing");}
                    });
                } catch (IOException io) {Platform.runLater(()->incorrect.setText("Can't find Server, Please check your IP Address"));}
                catch (ClassNotFoundException not) {not.printStackTrace();}
            }).start();
        });

        secondaryStage.setScene(new Scene(this,255,125));
        secondaryStage.setTitle("Skate board");
        secondaryStage.setResizable(false);
    }

    void showSecondStage(){
         secondaryStage.show();
    }
}

