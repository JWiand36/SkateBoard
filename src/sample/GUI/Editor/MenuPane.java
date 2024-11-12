package sample.GUI.Editor;

import javafx.scene.control.*;
import sample.Controller;
import sample.EventEditor;

import java.util.Optional;

//This pane displays the menu bar at the top
public class MenuPane extends MenuBar {

    //Sets up the MenuPane
    public MenuPane(Controller controller, EventEditor editor){

        Menu file = new Menu("Database");
        Menu message = new Menu("Message");
        Menu board = new Menu("Board");
        Menu help = new Menu("Help");
        MenuItem old = new MenuItem("Clear Expired Events");
        MenuItem newfile = new MenuItem("Remove All Events");
        MenuItem promotion = new MenuItem("Promotional");
        MenuItem alert = new MenuItem("Alert");
        MenuItem remove = new MenuItem("Remove Alert");
        MenuItem close = new MenuItem("Close Board");
        MenuItem show = new MenuItem("Show Board");
        MenuItem boardSettings = new MenuItem("Board Settings");
        MenuItem addInfo = new MenuItem("Add Event");
        MenuItem editInfo = new MenuItem("Edit Event");
        MenuItem removeInfo = new MenuItem("Remove Event");
        MenuItem messageInfo = new MenuItem("Message");
        MenuItem boardInfo = new MenuItem("Board");


        this.getMenus().addAll(file, message, board, help);
        file.getItems().addAll(old, newfile);
        message.getItems().addAll(promotion, alert, remove);
        board.getItems().addAll(show, close, boardSettings);
        help.getItems().addAll(addInfo, editInfo, removeInfo, messageInfo, boardInfo);

        //Closes the stage of the display board
        close.setOnAction(e->controller.closeBoard());

        //Shows the stage of the display board
        show.setOnAction(e->controller.showBoard());

        boardSettings.setOnAction(e->editor.setBottom(new BoardSettingsPane(controller)));

        old.setOnAction(e->controller.removeExpiredEvents());

        //Clears all events from the arraylist to start new
        newfile.setOnAction(e-> {
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Clear Events?");
            dialog.setHeaderText(null);
            dialog.setContentText("You are about to clear all events. Are you sure?");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK)
                controller.resetCollection();
            else
                e.consume();
        });

        //Sends a message that displays promotional message
        promotion.setOnAction(e-> editor.setBottom(new MessagePane(1, controller)));

        //Sends a message that displays any emergency messages
        alert.setOnAction(e-> editor.setBottom(new MessagePane(0, controller)));

        //Removes the alert message being displayed
        remove.setOnAction(e-> controller.removeAlertMessage());



        addInfo.setOnAction((e->{
            Alert infoMessage = new Alert(Alert.AlertType.INFORMATION);
            infoMessage.setTitle("Adding Events");
            infoMessage.setHeaderText("Adding Events");
            infoMessage.setContentText("To add an event fill out the text fields of the right side of the program. " +
                    "Fill in the teams, which locker they are assigned to and the rink number. Fill out what time the" +
                    "event takes place. Use the calendar button to pick the last day of the event (the current day is" +
                    "set by default). After everything has been filled out hit save.");
            infoMessage.show();
        }));

        editInfo.setOnAction((e->{
            Alert infoMessage = new Alert(Alert.AlertType.INFORMATION);
            infoMessage.setTitle("Editing Events");
            infoMessage.setHeaderText("Editing Events");
            infoMessage.setContentText("To edit an event right click the event on the table. Select edit event option" +
                    "and the text fields on the right side should populate. Change the desired fields and click save.");
            infoMessage.show();
        }));

        removeInfo.setOnAction((e->{
            Alert infoMessage = new Alert(Alert.AlertType.INFORMATION);
            infoMessage.setTitle("Removing Events");
            infoMessage.setHeaderText("Removing Events");
            infoMessage.setContentText("The program will automatically remove events when a new day starts and the last" +
                    "day of an event is before the current time. Although if you wish to remove an event, simply right" +
                    "click the event and select remove event option from the context menu.");
            infoMessage.show();
        }));

        messageInfo.setOnAction((e->{
            Alert infoMessage = new Alert(Alert.AlertType.INFORMATION);
            infoMessage.setTitle("Displaying Messages");
            infoMessage.setHeaderText("Displaying Messages");
            infoMessage.setContentText("If there is an issue and a message needs to be displayed. Clicking on message" +
                    "menu button will display a drop down. Select the desired type of message. Alerts will display in " +
                    "red, promotions will scroll across the bottom of the display board. When selected, the bottom of" +
                    "the editor will display a text fiend and the message type. Type what you need in the text field and" +
                    "click submit. To remove the alert message, click remove alert, the program will also automatically" +
                    "remove the alert when the next day starts.");
            infoMessage.show();
        }));

        boardInfo.setOnAction((e->{
            Alert infoMessage = new Alert(Alert.AlertType.INFORMATION);
            infoMessage.setTitle("Display Board");
            infoMessage.setHeaderText("Display Board");
            infoMessage.setContentText("There is little interaction with the display board, mainly it's used to just display" +
                    "information to the public. Although things can happen like the board being closed, to reopen the board," +
                    "simply click board in the menu and show board. If the board needs to be closed for any reason, click" +
                    "board in the menu and close board.");
            infoMessage.show();
        }));
    }


}
