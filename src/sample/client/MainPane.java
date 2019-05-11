package sample.client;


import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sample.Event;

import java.util.ArrayList;

//The MainPane of the client, it displays the days and allows you to pick your desired day.
class MainPane extends GridPane {

    private ListView<String>[] lists;
    private ListView<String> savedInfo = new ListView<>();
    private ArrayList<String> nameOfData = new ArrayList<>();
    private BorderPane fourthPane = new BorderPane();
    private Stage fourthStage = new Stage();
    private Client client;

    //Sets up the view of the MainPane
    MainPane(Client client) {

        this.setPadding(new Insets(5));
        this.setVgap(5);
        this.setHgap(5);
        this.client = client;

        lists = new ListView[7];
        for (int i = 0; i < lists.length; i++)
            lists[i] = new ListView<>();

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        Text[] texts = new Text[days.length];

        for(int i = 0; i < days.length; i++)
            texts[i] = new Text(days[i]);

        Button[] buttons = new Button[7];
        for (int i = 0; i < buttons.length; i++)
            buttons[i] = new Button("Modify");

        //Each button opens a ModifyPane to modify the desired day.
        buttons[0].setOnAction(e -> setModify(0));
        buttons[1].setOnAction(e -> setModify(1));
        buttons[2].setOnAction(e -> setModify(2));
        buttons[3].setOnAction(e -> setModify(3));
        buttons[4].setOnAction(e -> setModify(4));
        buttons[5].setOnAction(e -> setModify(5));
        buttons[6].setOnAction(e -> setModify(6));

        VBox[] panes = new VBox[7];
        for (int i = 0; i < lists.length; i++)
            panes[i] = new VBox(5);

        for (int i = 0; i < lists.length; i++) {
            panes[i].getChildren().addAll(texts[i], lists[i], buttons[i]);

            if (i < 4)
                this.add(panes[i], i, 0);
            else
                this.add(panes[i], i - 4, 1);
        }


        //This section is for the last ListView for data that will be saved to the computer after you close the program
        VBox savedPane = new VBox();
        HBox buttonsPane = new HBox();
        ComboBox<String> dates = new ComboBox<>();
        Button add = new Button("Add");
        Button remove = new Button("Remove");

        //Sets up how the ListView will be displayed
        for (Event event : client.getSavedData()) {
            if (event.getTeam2() != null)
                nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                        " " + event.getStartHour() + ":" + event.getStartMin() +
                        event.getDayNightCycle() + " R:" + event.getRinkNum());
            else
                nameOfData.add(event.getTeam1() +
                        " " + event.getStartHour() + ":" + event.getStartMin() +
                        event.getDayNightCycle() + " R:" + event.getRinkNum());
        }

        dates.getItems().addAll(FXCollections.observableArrayList(days));
        savedInfo.getItems().addAll(FXCollections.observableArrayList(nameOfData));

        buttonsPane.getChildren().addAll(add, remove);
        buttonsPane.setAlignment(Pos.TOP_CENTER);
        buttonsPane.setSpacing(5);

        savedPane.getChildren().addAll(dates, savedInfo, buttonsPane);
        savedPane.setAlignment(Pos.TOP_CENTER);
        savedPane.setSpacing(5);

        //Adds the selected data to the desired day
        add.setOnAction(e->{
            try {
                Event event = client.getSavedEvent(savedInfo.getSelectionModel().getSelectedIndex());
                client.addEvent(dates.getSelectionModel().getSelectedIndex(), event);
                if(event.getLocker1() == -1) {
                    event.setLocker1(0);
                    if(event.getTeam2() != null)
                        event.setLocker2(0);
                }
                client.sortEvents(false);
                client.updateLists(client.getEvents());
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list or choose a day.");}
        });

        //Allows the user to remove the data from the saved list
        remove.setOnAction(e->{
            try {
                client.removeSavedEvent(savedInfo.getSelectionModel().getSelectedIndex());
                nameOfData = new ArrayList<>();

                for (Event event : client.getSavedData()) {
                    if (event.getTeam2() != null)
                        nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                                " " + event.getStartHour() + ":" + event.getStartMin() +
                                event.getDayNightCycle() + " R:" + event.getRinkNum());
                    else
                        nameOfData.add(event.getTeam1() +
                                " " + event.getStartHour() + ":" + event.getStartMin() +
                                event.getDayNightCycle() + " R:" + event.getRinkNum());
                }

                savedInfo.setItems(FXCollections.observableArrayList(nameOfData));
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list or choose a day.");}
        });

        this.add(savedPane, 3, 1);


        fourthStage.setTitle("Modify Info");
        fourthStage.setScene(new Scene(fourthPane,750,660));
        fourthStage.setResizable(false);

    }

    private void setModify(int dayNumber){
        fourthStage.close();
        fourthPane.setCenter(new ModifyPane(dayNumber, client));
        fourthStage.show();
    }

    //Updates the lists on the MainPane
    void updateLists(ArrayList<Event>[] combinedEvents){

        ArrayList<String>[] names = new ArrayList[7];

        for (int i = 0; i < combinedEvents.length; i++) {
            names[i] = new ArrayList<>();

            for (int k = 0; k < combinedEvents[i].size(); k++)
                if(combinedEvents[i].get(k).getTeam2() != null)
                    names[i].add(combinedEvents[i].get(k).getTeam1() + " vs " + combinedEvents[i].get(k).getTeam2());
                else
                    names[i].add(combinedEvents[i].get(k).getTeam1());

            lists[i].setItems(FXCollections.observableArrayList(names[i]));
        }
    }

    ListView<String> getSavedInfo(){ return savedInfo; }
}
