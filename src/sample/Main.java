package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;


public class Main extends Application {

    private Controller controller;

    @Override
    public void start(Stage displayStage) {

        controller = new Controller();
        DisplayBoard board = new DisplayBoard();
        EventEditor editor = new EventEditor(controller);
        controller.setEditor(editor);
        controller.setBoard(board);
        controller.setDisplayStage(displayStage);
        controller.startClock();

        Scene scene = new Scene(board.getMainBorder(), 1100, 600);
        board.setScreenListeners(displayStage, scene);

        displayStage.setTitle("");
        displayStage.setScene(scene);
        displayStage.show();

        Scene editorScene = new Scene(editor, 1100, 600);
        Stage editorStage = new Stage();
        editorStage.setScene(editorScene);
        editorStage.show();

        editorStage.setOnCloseRequest(e->{
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Closing?");
            dialog.setHeaderText(null);
            dialog.setContentText("Closing this window will close the entire program \n including the display board. Are you sure?");
            dialog.initOwner(editorStage);

            Optional<ButtonType> result = dialog.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK)
                displayStage.close();
            else
                e.consume();
        });
    }



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop(){
        controller.stop();
        System.exit(0);
    }

}