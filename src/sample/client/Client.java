package sample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

import sample.Event;
import sample.board.FileIO;

/**
 * Created by John Wiand on 10/29/2016.
 */
public class Client extends Application {

    private ArrayList<Event> savedData = new ArrayList<>();

    private Stage secondaryStage;
    private Stage fourthStage;
    private ListView<String> savedInfo = new ListView<>();
    private ListView<String>[] lists;

    private EventCollection eventCollection;
    private NetworkService networkService;

    @Override
    public void start(Stage primaryStage){

        BorderPane mainPane;

        //The eventCollection is init here because it needs lists created which is done in MainPane
        MainPane pane = new MainPane();
        eventCollection = new EventCollection(this);
        networkService = new NetworkService(this, eventCollection);

        secondaryStage = new Stage();
        fourthStage = new Stage();

        pane.setPadding(new Insets(5));
        pane.setVgap(5);
        pane.setHgap(5);

        mainPane = new BorderPane();
        mainPane.setTop(new MenuPane(networkService, this, eventCollection));
        mainPane.setCenter(pane);

        new IPPane(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(mainPane, 600,650));

        try{
            networkService.setIPAddress((String) FileIO.readFromFile("IP"));
            networkService.inputData();
            primaryStage.show();
        }catch(IOException io){io.printStackTrace(); secondaryStage.show();}
        catch (ClassNotFoundException not){System.out.println("Error");}
    }

    //Displays the window asking for the Servers IP Address
    private class IPPane extends VBox{

        private IPPane(Stage primaryStage){


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
    }

    //The MainPane of the client, it displays the days and allows you to pick your desired day.
    class MainPane extends GridPane{

        ArrayList<String> nameOfData = new ArrayList<>();

        //Sets up the view of the MainPane
        private MainPane() {
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
            buttons[0].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(0);
            });
            buttons[1].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(1);
            });
            buttons[2].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(2);
            });
            buttons[3].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(3);
            });
            buttons[4].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(4);
            });
            buttons[5].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(5);
            });
            buttons[6].setOnAction(e -> {
                fourthStage.close();
                new ModifyPane(6);
            });

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

            try {
                savedData = (ArrayList<Event>) FileIO.readFromFile("Data");
            } catch (IOException |ClassNotFoundException e) {
                e.printStackTrace();
            }

            //Sets up how the ListView will be displayed
            for (Event event : savedData) {
                if (event.getTeam2() != null)
                    nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                            " " + event.getStartHour() + ":" + event.getStartMin() +
                            event.getDayNightCycle() + " L:" + event.getLocker1() + "/" +
                            event.getLocker2());
                else
                    nameOfData.add(event.getTeam1() +
                            " " + event.getStartHour() + ":" + event.getStartMin() +
                            event.getDayNightCycle() + " L:" + event.getLocker1());
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
                    Event event = savedData.get(savedInfo.getSelectionModel().getSelectedIndex());
                    eventCollection.addEvent(dates.getSelectionModel().getSelectedIndex(), event);
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list or choose a day.");}
            });

            //Allows the user to remove the data from the saved list
            remove.setOnAction(e->{
                try {
                    savedData.remove(savedInfo.getSelectionModel().getSelectedIndex());
                    nameOfData = new ArrayList<>();

                    for (Event event : savedData) {
                        if (event.getTeam2() != null)
                            nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                                    " " + event.getStartHour() + ":" + event.getStartMin() +
                                    event.getDayNightCycle() + " L:" + event.getLocker1() + "/" +
                                    event.getLocker2());
                        else
                            nameOfData.add(event.getTeam1() +
                                    " " + event.getStartHour() + ":" + event.getStartMin() +
                                    event.getDayNightCycle() + " L:" + event.getLocker1());
                    }

                    savedInfo.setItems(FXCollections.observableArrayList(nameOfData));
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list or choose a day.");}
            });

            this.add(savedPane, 3, 1);


        }
    }

    //This Pane allows the user to manipulate the data when the user selects the desired day
    class ModifyPane extends GridPane{

        ArrayList<String> n;
        ListView<String> list;

        //Sets up the view of the Modify Pane. It creates 20 rows of TextFields to allow the user to enter in multiple
        //events at once
        private ModifyPane(int modifyingDay){

            list = new ListView<>();

            update(modifyingDay);

            ColumnConstraints col1 = new ColumnConstraints(200);
            ColumnConstraints col2 = new ColumnConstraints(140);
            ColumnConstraints col3 = new ColumnConstraints(50);
            ColumnConstraints col4 = new ColumnConstraints(140);
            ColumnConstraints col5 = new ColumnConstraints(50);
            ColumnConstraints col6 = new ColumnConstraints(50);
            ColumnConstraints col7 = new ColumnConstraints(50);
            ColumnConstraints col8 = new ColumnConstraints(50);

            FlowPane flow = new FlowPane();

            Button add = new Button("Add");
            Button modify = new Button("Modify");
            Button remove = new Button("Remove");
            Button save = new Button("Save");
            Text[] t = {new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker"), new Text("Hour"),
                    new Text("Minute"), new Text("Am?")};
            TextField[][] tf = new TextField[20][6];
            CheckBox[] ampm = new CheckBox[20];

            //Checks the info and adds the data if it meets the requirements
            add.setOnAction(e->{
                for(int i = 0; i < tf.length; i++) {
                    if (checkInfoAdd(tf[i], n, ampm[i].isSelected())) {
                        eventCollection.addEvent(modifyingDay, modifyInfo(tf[i], ampm[i]));
                        update(modifyingDay);
                    }
                }
            });

            //The user can select data from the list and modify it. It only uses the top row of TextFields
            modify.setOnAction(e->{
                try {
                    if (checkInfo(tf[0])) {
                        int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                        eventCollection.removeEvent(modifyingDay, selected);
                        eventCollection.addEvent(modifyingDay, modifyInfo(tf[0], ampm[0]));
                        update(modifyingDay);
                    }
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list.");}
            });

            //The user can select data from the list and remove it.
            remove.setOnAction(e->{
                try {
                    int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                    eventCollection.removeEvent(modifyingDay, selected);
                    update(modifyingDay);
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list.");}
            });

            //Sets the look of the text in the ListView
            list.getSelectionModel().selectedItemProperty().addListener(e-> {

//new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker"), new Text("Hour"),new Text("Minute"), new Text("Am?")
                try {
                    int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                    Event event = eventCollection.getEvent(modifyingDay, selected);
                    tf[0][0].setText(event.getTeam1());
                    tf[0][1].setText(event.getLocker1() + "");
                    tf[0][2].setText(event.getTeam2());
                    tf[0][3].setText(event.getLocker2() + "");
                    tf[0][4].setText(event.getStartHour() + "");
                    tf[0][5].setText(event.getStartMin() + "");
                    if (event.getDayNightCycle().equals("am"))
                        ampm[0].selectedProperty().setValue(true);
                    else
                        ampm[0].selectedProperty().setValue(false);
                }catch (ArrayIndexOutOfBoundsException IOB){IOB.getStackTrace();}
            });

            //Allows the user to store data in the SavedList to be used for another day
            save.setOnAction(e->{
                try {
                    if (checkInfoAdd(tf[0], savedData, ampm[0].isSelected())) {
                        savedData.add(modifyInfo(tf[0], ampm[0]));
                        ArrayList<String> nameOfData = new ArrayList<>();

                        for (Event event : savedData) {
                            if (event.getTeam2() != null)
                                nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " L:" + event.getLocker1() + "/" +
                                        event.getLocker2());
                            else
                                nameOfData.add(event.getTeam1() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " L:" + event.getLocker1());
                        }

                        savedInfo.setItems(FXCollections.observableArrayList(nameOfData));
                    }

                }catch (ArrayIndexOutOfBoundsException out){out.getStackTrace();}
            });

            flow.getChildren().addAll(add,modify,remove, save);
            flow.setHgap(5);

            this.setPadding(new Insets(5));
            this.setHgap(5);
            this.setVgap(5);
            setRowSpan(list, REMAINING);
            setColumnSpan(flow, REMAINING);
            this.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6, col7, col8);

            this.add(list,0,0);

            this.add(flow, 1, tf.length+2);

            for(int i = 1; i < t.length; i++)
                this.add(t[i - 1], i, 0);


            for(int i = 0; i < tf.length; i++){
                for(int k = 1; k <= tf[i].length; k++) {
                    tf[i][k - 1] = new TextField();
                    this.add(tf[i][k - 1], k, i + 1);
                }
            }

            for(int i = 0; i < ampm.length; i++){
                ampm[i] = new CheckBox();
                this.add(ampm[i],7,i+1);
            }

            this.add(t[6],7,0);

            fourthStage.setTitle("Modify Info");
            fourthStage.setScene(new Scene(this,750,660));
            fourthStage.setResizable(false);
            fourthStage.show();
        }

        //Checks the data when the user hits the add button. Data can't match other data and the list and it has to meet
        //certain requirements. Team 1 needs to be filled, as Locker Room 1, Hour, Min are all required
        //The data is matched with the ListView's String values to see if the event matches
        private boolean checkInfoAdd(TextField[] tf, ArrayList n, Boolean ampm){

            //Team1, Locker1, Team2, Locker2, Hour, Min,

            String am;

            //Checks if if their are vales to meet requirements
            if(tf[1].getText() != null && tf[4].getText() != null && tf[5].getText() != null &&
                    tf[1].getText().length() > 0 && tf[4].getText().length() > 0 && tf[5].getText().length() > 0&&
                    isNumber(tf[1].getText()) && isNumber(tf[4].getText()) && isNumber(tf[5].getText())){

                int l1 = Integer.parseInt(tf[1].getText());
                int h = Integer.parseInt(tf[4].getText());
                int m = Integer.parseInt(tf[5].getText());

                if(ampm)
                    am = "am";
                else
                    am = "pm";

                //Checks to see if Team 2 and Locker Room 2 are filled, if not the program passes
                if(tf[3].getText() != null && tf[3].getText().length() > 0 &&
                        tf[2].getText() != null && tf[2].getText().length() > 0){
                    if(isNumber(tf[3].getText())) {
                        int l2 = Integer.parseInt(tf[3].getText());

                        if (l2 < 1 || l2 > 8 || n.contains(h+":"+m+am+" "+tf[0].getText()+" L"+tf[1].getText()+" vs "+tf[2].getText()+
                                " L"+tf[3].getText()))
                            return false;
                    }else
                        return false;
                }

                //Checks if the locker Rooms are in range and if the time is correct
                return l1 >= 1 && l1 <= 8 && h >= 0 && h <= 24 && m >= 0 && m <= 60 && !n.contains(h + ":" + m + am + " " + tf[0].getText() + " L" + tf[1].getText());
            }else
                return false;
        }

        //Same as CheckInfoAdd with minor differences, it doesn't check for Am/Pm. Used for the Modify Button
        private boolean checkInfo(TextField[] tf){

            //Team1, Locker1, Team2, Locker2, Hour, Min,

            //Checks to see if Team1, Locker1, Hour and Min, all have values
            if(tf[1].getText() != null && tf[4].getText() != null && tf[5].getText() != null &&
                    tf[1].getText().length() > 0 && tf[4].getText().length() > 0 && tf[5].getText().length() > 0&&
                    isNumber(tf[1].getText()) && isNumber(tf[4].getText()) && isNumber(tf[5].getText())){

                int l1 = Integer.parseInt(tf[1].getText());
                int h = Integer.parseInt(tf[4].getText());
                int m = Integer.parseInt(tf[5].getText());

                //Checks to see if there is a Team2 or Locker2
                if(tf[3].getText() != null && tf[3].getText().length() > 0 &&
                        tf[2].getText() != null && tf[2].getText().length() > 0){
                    if(isNumber(tf[3].getText())) {
                        int l2 = Integer.parseInt(tf[3].getText());

                        if (l2 < 1 || l2 > 8)
                            return false;
                    }else
                        return false;
                }

                //Checks Locker1 and time
                return l1 >= 1 && l1 <= 8 && h >= 0 && h <= 24 && m >= 0 && m <= 60;
            }else
                return false;
        }

        //Manipulates the data's values and displays it in the ListView
        private Event modifyInfo(TextField[] tf, CheckBox ampm) {

            String team1 = tf[0].getText();
            String am;
            int locker1 = Integer.parseInt(tf[1].getText());
            int hour = Integer.parseInt(tf[4].getText());
            int min = Integer.parseInt(tf[5].getText());

            if (ampm.isSelected())
                am = "am";
            else
                am = "pm";

            //Checks if there is a second Team
            if (tf[2].getText() != null && tf[3].getText() != null){
                if (tf[2].getText().length() > 0 && tf[3].getText().length() > 0) {
                    String team2 = tf[2].getText();
                    int locker2 = Integer.parseInt(tf[3].getText());

                    for (TextField field: tf)
                        field.setText(null);

                    ampm.selectedProperty().setValue(false);

                    hour %= 12;

                    if (hour == 0)
                        hour = 12;

                    return new Event(team1, team2, locker1, locker2, hour, min, am);
                } else {

                    for (TextField field: tf)
                        field.setText(null);

                    ampm.selectedProperty().setValue(false);

                    hour %= 12;

                    if (hour == 0)
                        hour = 12;

                    return new Event(team1, locker1, hour, min, am);
                }

                //If there isn't a second event
            }else {

                for (TextField field: tf)
                    field.setText(null);

                ampm.selectedProperty().setValue(false);

                hour %= 12;

                if(hour == 0)
                    hour = 12;

                return new Event(team1, locker1, hour, min, am);
            }
        }

        //Used to update the list, sets up the String for the ListView
        private void update(int d){

            n = new ArrayList<>();

            String time;

            ArrayList<Event>[] combinedEvents = eventCollection.getEvents();

            for(int i = 0; i < combinedEvents[d].size(); i++) {

                time = combinedEvents[d].get(i).getStartHour() + ":" + combinedEvents[d].get(i).getStartMin() +
                        combinedEvents[d].get(i).getDayNightCycle() + " ";

                if (combinedEvents[d].get(i).getTeam2() != null)
                    n.add(time+combinedEvents[d].get(i).getTeam1() + " L" + combinedEvents[d].get(i).getLocker1() +
                            " vs " + combinedEvents[d].get(i).getTeam2() + " L" + combinedEvents[d].get(i).getLocker2());
                else
                    n.add(time+combinedEvents[d].get(i).getTeam1() + " L" + combinedEvents[d].get(i).getLocker1());
            }

            list.setItems(FXCollections.observableArrayList(n));
            updateLists(combinedEvents);
        }

        //Checks Locker Room1, Locker Room2, Hour and Min TextFields are numbers
        private boolean isNumber(String s){

            char a;
            int c = 0;

            for(int i = 0; i < s.length(); i++){

                a = s.charAt(i);

                if(!Character.isDigit(a))
                    c++;
            }

            return c <= 0;
        }
    }

    //Displays if the user makes a mistake
    private void displayError(String message){
        Stage s = new Stage();

        Text t = new Text(message);
        t.setWrappingWidth(225);

        Button b = new Button("Ok");
        b.setOnAction(e->s.close());

        VBox p = new VBox(t, b);
        p.setAlignment(Pos.CENTER);
        p.setSpacing(10);

        s.setScene(new Scene(p, 250, 100));
        s.setTitle("Error Message!!");
        s.setResizable(false);
        s.show();
    }

    ArrayList<Event> getSavedData(){
        return savedData;
    }

    void setCombinedEvents(ArrayList<Event>[] events){ this.eventCollection.setEvents(events); }

    void showSecondWindow(){
        secondaryStage.show();
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

    @Override
    public void stop(){
        try {
            FileIO.writeToFile(savedData, "Data");
        }catch (IOException io){io.printStackTrace();}
        System.exit(0);
    }

}