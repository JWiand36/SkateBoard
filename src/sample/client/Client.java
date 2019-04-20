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
import java.net.Socket;
import java.util.ArrayList;

import sample.Event;
import sample.board.FileIO;
import sample.board.Main;

/**
 * Created by John Wiand on 10/29/2016.
 */
public class Client extends Application {

    private String ip = "localhost";

    private ArrayList<Event> savedData = new ArrayList<>();

    private Stage secondaryStage;
    private Stage thirdStage;
    private Stage fourthStage;
    private ArrayList<Event>[] combinedEvents;
    private ListView<String> savedInfo = new ListView<>();
    private ListView<String>[] lists;

    @Override
    public void start(Stage primaryStage){

        BorderPane mainPane;
        MainPane pane;

        combinedEvents = new ArrayList[7];
        for(int i = 0; i < combinedEvents.length; i++)
            combinedEvents[i] = new ArrayList<>();

        secondaryStage = new Stage();
        thirdStage = new Stage();
        fourthStage = new Stage();

        pane = new MainPane();
        pane.setPadding(new Insets(5));
        pane.setVgap(5);
        pane.setHgap(5);

        mainPane = new BorderPane();
        mainPane.setTop(new MenuPane());
        mainPane.setCenter(pane);

        new IPPane(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(mainPane, 600,650));

        try{
            ip = (String) FileIO.readFromFile("IP");
            inputData(ip);
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
                ip = ipTF.getText();

                new Thread(()-> {
                    try {
                        inputData(ip);
                        Platform.runLater(()->{
                            secondaryStage.close();
                            primaryStage.show();
                            incorrect.setText("");
                            try {
                                FileIO.writeToFile(ip, "IP");
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

    //This pane displays the menu bar at the top
    class MenuPane extends MenuBar{

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

        //Sets up the MenuPane
        private MenuPane(){
            this.getMenus().addAll(file, server, message);
            file.getItems().addAll(newfile,exit);
            server.getItems().addAll(input, output, change);
            message.getItems().addAll(promotion, alert, remove);

            //Clears all events from the arraylist to start new
            newfile.setOnAction(e->{
                combinedEvents = new ArrayList[7];

                for(int i = 0; i < combinedEvents.length; i++)
                    combinedEvents[i] = new ArrayList<>();

                updateLists(combinedEvents);
            });

            //Exits the project and saves the data in the saved array
            exit.setOnAction(e->{
                try {
                    FileIO.writeToFile(savedData, "Data");
                }catch (IOException io){io.printStackTrace();}
                System.exit(0);
            });

            //Retrieves data from the server
            input.setOnAction(e -> {
                try {
                    inputData(ip);
                } catch (IOException io) {secondaryStage.show();
                } catch (ClassNotFoundException not) {not.printStackTrace();}
            });

            //Sends data to the server
            output.setOnAction(e -> {
                try {
                    outputData(ip);
                } catch (IOException io) {secondaryStage.show();}
            });

            //Allows the user to change the server
            change.setOnAction(e-> secondaryStage.show());

            //Sends a message that displays promotional message
            promotion.setOnAction(e-> new MessagePane(1));

            //Sends a message that displays any emergency messages
            alert.setOnAction(e-> new MessagePane(0));

            //Removes the alert message being displayed
            remove.setOnAction(e-> {
                try {
                    removeAlertMessage(ip);
                } catch (IOException io) {secondaryStage.show();}
            });
        }

        //Sends the data to the Server
        private void outputData(String ip) throws IOException {
            Socket socket = new Socket(ip, 36);

            //The streams are in order with the server. Even if it isn't used
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

            output.writeByte(2);
            objOutput.writeObject(separateEvents(combinedEvents));

            objOutput.flush();
            objOutput.close();
            socket.close();
        }

        //Removes the alert message on the Server
        private void removeAlertMessage(String ip) throws IOException {
            try {
                Socket socket = new Socket(ip, 36);

                //The streams are in order with the server. Even if it isn't used
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

                output.writeByte(4);
                socket.close();


            } catch (IOException io) {io.printStackTrace();}

        }

        //The Event list coming from the server is 14 different ArrayLists but the client uses only 7, this method
        //separates the ArrayLists based on which rink they are used. Makes it easier to handle the data for the Server
        private ArrayList<Event>[] separateEvents(ArrayList<Event>[] combinedEvents){

            //Just checks if the first locker room is higher then 4. If the first locker is higher then 4 then the Event
            //takes place on Rink 2, otherwise the Event is on Rink 1.
            ArrayList<Event>[] result = new ArrayList[14];

            for(int i = 0; i < result.length; i++)
                result[i] = new ArrayList<>();

            for(int i = 0; i < combinedEvents.length; i++){
                for(int k = 0; k < combinedEvents[i].size(); k++){

                    if(combinedEvents[i].get(k).getLocker1()<5)
                        result[i].add(combinedEvents[i].get(k));
                    else
                        result[i+7].add(combinedEvents[i].get(k));

                }
            }

            return result;
        }
    }

    //Sets up the MessagePane for both the Promotional message and the Alert message
    class MessagePane extends VBox{

        Text t;
        TextField tf;
        Button submit;

        private MessagePane(int typeOfMessage){

            t = new Text();
            tf = new TextField();
            submit = new Button("Submit");

            if(typeOfMessage == 1) {
                t.setText("The Promotional Message");

                submit.setOnAction(e -> {
                    try {
                        sendMessage(ip, tf.getText());
                        Platform.runLater(()->thirdStage.close());
                    } catch (IOException io) {secondaryStage.show();}
                });
            }else {
                t.setText("The Alert Message");

                submit.setOnAction(e -> {
                    try {
                        sendAlertMessage(ip, tf.getText());
                        Platform.runLater(()->thirdStage.close());
                    } catch (IOException io) {secondaryStage.show();}
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

        //Sends the Alert message to the server
        private void sendAlertMessage(String ip, String message) throws IOException {
            try {
                Socket socket = new Socket(ip, 36);

                //The streams are in order with the server. Even if it isn't used
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

                output.writeByte(3);
                objOutput.writeObject(message);

                objOutput.flush();
                objOutput.close();
                socket.close();
            }catch (IOException io){io.printStackTrace();}
        }

        //Sends a Promotional message to the server
        private void sendMessage(String ip, String message)throws IOException {
            try {
                Socket socket = new Socket(ip, 36);

                //The streams are in order with the server. Even if it isn't used
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

                output.writeByte(5);
                objOutput.writeObject(message);

                objOutput.flush();
                objOutput.close();
                socket.close();
            }catch (IOException io){io.printStackTrace();}
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
                    combinedEvents[dates.getSelectionModel().getSelectedIndex()].add(event);
                    updateLists(combinedEvents);
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
                        combinedEvents[modifyingDay].add(modifyInfo(tf[i], ampm[i]));
                        update(modifyingDay);
                    }
                }
            });

            //The user can select data from the list and modify it. It only uses the top row of TextFields
            modify.setOnAction(e->{
                try {
                    if (checkInfo(tf[0])) {
                        int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                        combinedEvents[modifyingDay].remove(selected);
                        combinedEvents[modifyingDay].add(modifyInfo(tf[0], ampm[0]));
                        update(modifyingDay);
                    }
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list.");}
            });

            //The user can select data from the list and remove it.
            remove.setOnAction(e->{
                try {
                    int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                    combinedEvents[modifyingDay].remove(selected);
                    update(modifyingDay);
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list.");}
            });

            //Sets the look of the text in the ListView
            list.getSelectionModel().selectedItemProperty().addListener(e-> {
                try {
                    int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                    tf[0][0].setText(combinedEvents[modifyingDay].get(selected).getTeam1());
                    tf[0][1].setText(combinedEvents[modifyingDay].get(selected).getLocker1() + "");
                    tf[0][2].setText(combinedEvents[modifyingDay].get(selected).getTeam2());
                    tf[0][3].setText(combinedEvents[modifyingDay].get(selected).getLocker2() + "");
                    tf[0][4].setText(combinedEvents[modifyingDay].get(selected).getStartHour() + "");
                    tf[0][5].setText(combinedEvents[modifyingDay].get(selected).getStartMin() + "");
                    if (combinedEvents[modifyingDay].get(selected).getDayNightCycle().equals("am"))
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

    //Retrieves the data from the server
    private void inputData(String ip) throws IOException, ClassNotFoundException {

        Socket socket = new Socket(ip, 36);

        //The streams are in order with the server. Even if it isn't used
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(1);
        combinedEvents = combineEvents((ArrayList<Event>[]) objInput.readObject());
        updateLists(combinedEvents);
        socket.close();

    }

    //The client manipulates the data with 7 ArrayLists but the Server uses 14 ArrayLists, makes it easier to manipulate
    //the data. This combines the data into 7 ArrayLists
    private ArrayList<Event>[] combineEvents(ArrayList<Event>[] events){

        ArrayList<Event>[] result = new ArrayList[7];

        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < events.length; i++) {
            for (int k = 0; k < events[i].size(); k++)
                result[i % 7].add(events[i].get(k));
        }

        return result;
    }

    //Updates the lists on the MainPane
    private void updateLists(ArrayList<Event>[] combinedEvents){

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

    @Override
    public void stop(){
        try {
            FileIO.writeToFile(savedData, "Data");
        }catch (IOException io){io.printStackTrace();}
        System.exit(0);
    }

}