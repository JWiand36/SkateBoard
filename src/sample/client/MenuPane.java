package sample.client;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import sample.Event;
import sample.board.FileIO;

import java.io.IOException;
import java.util.ArrayList;

//This pane displays the menu bar at the top
class MenuPane extends MenuBar {

    //Sets up the MenuPane
    MenuPane(NetworkService networkService, Client client, EventCollection eventCollection){

        Menu file = new Menu("File");
        Menu server = new Menu("Server");
        Menu message = new Menu("Message");
        MenuItem newfile = new MenuItem("New");
        MenuItem exit = new MenuItem("Exit");
        MenuItem input = new MenuItem("Import");
        MenuItem output = new MenuItem("Export");
        MenuItem change = new MenuItem("Change Server");
        MenuItem promotion = new MenuItem("Promotional");
        MenuItem alert = new MenuItem("Alert");
        MenuItem remove = new MenuItem("Remove Alert");


        this.getMenus().addAll(file, server, message);
        file.getItems().addAll(newfile,exit);
        server.getItems().addAll(input, output, change);
        message.getItems().addAll(promotion, alert, remove);

        //Clears all events from the arraylist to start new
        newfile.setOnAction(e->{
            eventCollection.resetCollection();
        });

        //Exits the project and saves the data in the saved array
        exit.setOnAction(e->{
            try {
                FileIO.writeToFile(client.getSavedData(), "Data");
            }catch (IOException io){io.printStackTrace();}
            System.exit(0);
        });

        //Retrieves data from the server
        input.setOnAction(e -> {
            try {
                networkService.inputData();
            } catch (IOException io) {client.showSecondWindow();
            } catch (ClassNotFoundException not) {not.printStackTrace();}
        });

        //Sends data to the server
        output.setOnAction(e -> {
            try {
                networkService.outputData();
            } catch (IOException io) {client.showSecondWindow();}
        });

        //Allows the user to change the server
        change.setOnAction(e-> client.showSecondWindow());

        //Sends a message that displays promotional message
        promotion.setOnAction(e-> new MessagePane(1, networkService, client));

        //Sends a message that displays any emergency messages
        alert.setOnAction(e-> new MessagePane(0, networkService, client));

        //Removes the alert message being displayed
        remove.setOnAction(e-> {
            try {
                networkService.removeAlertMessage();
            } catch (IOException io) {client.showSecondWindow();}
        });
    }


}
