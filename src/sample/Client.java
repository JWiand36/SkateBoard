package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by John Wiand on 10/29/2016.
 */
public class Client extends Application {

    private String ip = "localhost";

    ArrayList<Event> savedData = new ArrayList<>();

    private Stage secondaryStage;
    private Stage thirdStage;
    private Stage fourthStage;
    private ArrayList<Event>[] combinedEvents;
    private ListView<String> savedInfo = new ListView<>();
    private BorderPane mainPane;
    private MainPane pane;
    private ListView<String>[] lists;

    @Override
    public void start(Stage primaryStage){

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
            ip = (String)readFromFile("IP");
            inputData(ip);
            primaryStage.show();
        }catch(IOException io){io.printStackTrace(); secondaryStage.show();}
        catch (ClassNotFoundException not){System.out.println("Error");}
    }

    class IPPane extends VBox{

        private IPPane(Stage primaryStage){


            Text incorrect = new Text();
            Text ipText = new Text("What is the Ip Address of the Server?");
            TextField ipTF = new TextField();
            Button connect = new Button("Connect");

            this.getChildren().addAll(incorrect,ipText,ipTF,connect);
            this.setPadding(new Insets(5));
            this.setAlignment(Pos.TOP_CENTER);

            incorrect.setStyle("-fx-fill: Red;");

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
                                writeToFile(ip, "IP");
                            }catch (IOException io){io.printStackTrace();System.out.println("Error Writing");}
                        });
                    } catch (IOException io) {Platform.runLater(()->incorrect.setText("Can't find Server, Please check your IP Address"));}
                    catch (ClassNotFoundException not) {not.printStackTrace();}
                }).start();
            });

            secondaryStage.setScene(new Scene(this,255,125));
            secondaryStage.setTitle("Skate Board");
            secondaryStage.setResizable(false);
        }
    }

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

        private MenuPane(){
            this.getMenus().addAll(file, server, message);
            file.getItems().addAll(newfile,exit);
            server.getItems().addAll(input, output, change);
            message.getItems().addAll(promotion, alert, remove);

            newfile.setOnAction(e->{
                combinedEvents = new ArrayList[7];

                for(int i = 0; i < combinedEvents.length; i++)
                    combinedEvents[i] = new ArrayList<Event>();

                updateLists(combinedEvents);
            });

            exit.setOnAction(e->{
                try {
                    writeToFile(savedData, "Data");
                }catch (IOException io){io.printStackTrace();}
                System.exit(0);
            });

            input.setOnAction(e -> {
                new Thread(()->{
                    try {
                        inputData(ip);
                    } catch (IOException io) {secondaryStage.show();
                    } catch (ClassNotFoundException not) {not.printStackTrace();}
                }).start();
            });

            output.setOnAction(e -> {
                new Thread(()-> {
                    try {
                        outputData(ip);
                    } catch (IOException io) {secondaryStage.show();}
                }).start();
            });

            change.setOnAction(e->{
                secondaryStage.show();
            });

            promotion.setOnAction(e->{
                new Thread(()->{
                    new MessagePane(1);
                }).start();
            });

            alert.setOnAction(e->{
                new Thread(()->{
                    new MessagePane(0);
                }).start();
            });

            remove.setOnAction(e->{
                new Thread(()->{
                    try {
                        removeAlertMessage(ip);
                    }catch (IOException io){secondaryStage.show();}
                }).start();
            });
        }

        private void outputData(String ip) throws IOException {
            Socket socket = new Socket(ip, 36);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

            output.writeByte(2);
            objOutput.writeObject(separateEvents(combinedEvents));

            objOutput.flush();
            objOutput.close();
            socket.close();
        }

        private void removeAlertMessage(String ip) throws IOException {
            try {
                Socket socket = new Socket(ip, 36);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

                output.writeByte(4);
                socket.close();


            } catch (IOException io) {io.printStackTrace();}

        }

        private ArrayList<Event>[] separateEvents(ArrayList<Event>[] combinedEvents){

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
                    new Thread(() -> {
                        try {
                            sendMessage(ip, tf.getText());
                            Platform.runLater(()->thirdStage.close());
                        } catch (IOException io) {secondaryStage.show();}
                    }).start();
                });
            }else {
                t.setText("The Alert Message");

                submit.setOnAction(e -> {
                    new Thread(() -> {
                        try {
                            sendAlertMessage(ip, tf.getText());
                            Platform.runLater(()->thirdStage.close());
                        } catch (IOException io) {secondaryStage.show();}
                    }).start();
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

        private void sendAlertMessage(String ip, String message) throws IOException {
            try {
                Socket socket = new Socket(ip, 36);
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

        private void sendMessage(String ip, String message)throws IOException {
            try {
                Socket socket = new Socket(ip, 36);
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

    class MainPane extends GridPane{

        ArrayList<String> nameOfData = new ArrayList<>();

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

            VBox savedPane = new VBox();
            HBox buttonsPane = new HBox();
            ComboBox<String> dates = new ComboBox<>();
            Button add = new Button("Add");
            Button remove = new Button("Remove");

            try {
                savedData = (ArrayList<Event>) readFromFile("Data");
            } catch (IOException io) {
                io.printStackTrace();
            } catch (ClassNotFoundException not) {
                not.printStackTrace();
            }

            for (int i = 0; i < savedData.size(); i++) {
                if (savedData.get(i).getTeam2() != null)
                    nameOfData.add(savedData.get(i).getTeam1() + " vs " + savedData.get(i).getTeam2() +
                            " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                            savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1() + "/" +
                            savedData.get(i).getLocker2());
                else
                    nameOfData.add(savedData.get(i).getTeam1() +
                            " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                            savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1());
            }

            dates.getItems().addAll(FXCollections.observableArrayList(days));
            savedInfo.getItems().addAll(FXCollections.observableArrayList(nameOfData));

            buttonsPane.getChildren().addAll(add, remove);
            buttonsPane.setAlignment(Pos.TOP_CENTER);
            buttonsPane.setSpacing(5);

            savedPane.getChildren().addAll(dates, savedInfo, buttonsPane);
            savedPane.setAlignment(Pos.TOP_CENTER);
            savedPane.setSpacing(5);

            add.setOnAction(e->{
                try {
                    Event event = savedData.get(savedInfo.getSelectionModel().getSelectedIndex());
                    combinedEvents[dates.getSelectionModel().getSelectedIndex()].add(event);
                    updateLists(combinedEvents);
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list or choose a day.");}
            });

            remove.setOnAction(e->{
                try {
                    savedData.remove(savedInfo.getSelectionModel().getSelectedIndex());
                    nameOfData = new ArrayList<String>();

                    for (int i = 0; i < savedData.size(); i++) {
                        if (savedData.get(i).getTeam2() != null)
                            nameOfData.add(savedData.get(i).getTeam1() + " vs " + savedData.get(i).getTeam2() +
                                    " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                                    savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1() + "/" +
                                    savedData.get(i).getLocker2());
                        else
                            nameOfData.add(savedData.get(i).getTeam1() +
                                    " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                                    savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1());
                    }

                    savedInfo.setItems(FXCollections.observableArrayList(nameOfData));
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list or choose a day.");}
            });

            this.add(savedPane, 3, 1);


        }
    }

    class ModifyPane extends GridPane{

        ArrayList<String> n;
        ListView<String> list;

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


            add.setOnAction(e->{
                for(int i = 0; i < tf.length; i++) {
                    if (checkInfoAdd(tf[i], n, ampm[i].isSelected())) {
                        combinedEvents[modifyingDay].add(modifyInfo(tf[i], ampm[i]));
                        update(modifyingDay);
                    }
                }
            });

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

            remove.setOnAction(e->{
                try {
                    int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                    combinedEvents[modifyingDay].remove(selected);
                    update(modifyingDay);
                }catch (ArrayIndexOutOfBoundsException out){displayError("Select an Event from the list.");}
            });

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
                }catch (ArrayIndexOutOfBoundsException IOB){}
            });

            save.setOnAction(e->{
                try {
                    if (checkInfoAdd(tf[0], savedData, ampm[0].isSelected())) {
                        savedData.add(modifyInfo(tf[0], ampm[0]));
                        ArrayList<String> nameOfData = new ArrayList<String>();

                        for (int i = 0; i < savedData.size(); i++)
                            if(savedData.get(i).getTeam2() != null)
                                nameOfData.add(savedData.get(i).getTeam1() + " vs " + savedData.get(i).getTeam2() +
                                    " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                                    savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1() + "/" +
                                    savedData.get(i).getLocker2());
                            else
                                nameOfData.add(savedData.get(i).getTeam1() +
                                        " " + savedData.get(i).getStartHour() + ":" + savedData.get(i).getStartMin() +
                                        savedData.get(i).getDayNightCycle() + " L:" + savedData.get(i).getLocker1());

                        savedInfo.setItems(FXCollections.observableArrayList(nameOfData));
                    }
                }catch (ArrayIndexOutOfBoundsException out){}
            });

            flow.getChildren().addAll(add,modify,remove, save);
            flow.setHgap(5);

            this.setPadding(new Insets(5));
            this.setHgap(5);
            this.setVgap(5);
            this.setRowSpan(list, REMAINING);
            this.setColumnSpan(flow, REMAINING);
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

        private boolean checkInfoAdd(TextField[] tf, ArrayList n, Boolean ampm){

            //Team1, Locker1, Team2, Locker2, Hour, Min,

            String am;

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

                if(l1 < 1 || l1 > 8 || h < 0 || h > 24 || m < 0 || m > 60 || n.contains(h+":"+m+am+" "+tf[0].getText()+" L"+tf[1].getText()))
                        return false;
                return true;
            }else
                return false;
        }

        private boolean checkInfo(TextField[] tf){

            //Team1, Locker1, Team2, Locker2, Hour, Min,

            if(tf[1].getText() != null && tf[4].getText() != null && tf[5].getText() != null &&
                    tf[1].getText().length() > 0 && tf[4].getText().length() > 0 && tf[5].getText().length() > 0&&
                    isNumber(tf[1].getText()) && isNumber(tf[4].getText()) && isNumber(tf[5].getText())){

                int l1 = Integer.parseInt(tf[1].getText());
                int h = Integer.parseInt(tf[4].getText());
                int m = Integer.parseInt(tf[5].getText());

                if(tf[3].getText() != null && tf[3].getText().length() > 0 &&
                        tf[2].getText() != null && tf[2].getText().length() > 0){
                    if(isNumber(tf[3].getText())) {
                        int l2 = Integer.parseInt(tf[3].getText());

                        if (l2 < 1 || l2 > 8)
                            return false;
                    }else
                        return false;
                }

                if(l1 < 1 || l1 > 8 || h < 0 || h > 24 || m < 0 || m > 60)
                    return false;
                return true;
            }else
                return false;
        }

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

            if (tf[2].getText() != null && tf[3].getText() != null)
                if (tf[2].getText().length() > 0 && tf[3].getText().length() > 0) {
                    String team2 = tf[2].getText();
                    int locker2 = Integer.parseInt(tf[3].getText());

                    for (int i = 0; i < tf.length; i++)
                        tf[i].setText(null);

                    ampm.selectedProperty().setValue(false);

                    hour %= 12;

                    if(hour == 0)
                        hour = 12;

                    return new Event(team1, team2, locker1, locker2, hour, min, am);
                } else {

                    for (int i = 0; i < tf.length; i++)
                        tf[i].setText(null);

                    ampm.selectedProperty().setValue(false);

                    hour %= 12;

                    if(hour == 0)
                        hour = 12;

                    return new Event(team1, locker1, hour, min, am);
                }
            else {

                for (int i = 0; i < tf.length; i++)
                    tf[i].setText(null);

                ampm.selectedProperty().setValue(false);

                hour %= 12;

                if(hour == 0)
                    hour = 12;

                return new Event(team1, locker1, hour, min, am);
            }
        }

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

        private boolean isNumber(String s){

            char a;
            int c = 0;

            for(int i = 0; i < s.length(); i++){

                a = s.charAt(i);

                if(!Character.isDigit(a))
                    c++;
            }

            if(c > 0)
                return false;
            else
                return true;
        }
    }

    private void inputData(String ip) throws IOException, ClassNotFoundException {

        Socket socket = new Socket(ip, 36);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(1);
        combinedEvents = combineEvents((ArrayList<Event>[]) objInput.readObject());
        updateLists(combinedEvents);
        socket.close();

    }

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

    private void updateLists(ArrayList<Event>[] combinedEvents){

        ArrayList<String>[] names = new ArrayList[7];

        for (int i = 0; i < combinedEvents.length; i++) {
            names[i] = new ArrayList();

            for (int k = 0; k < combinedEvents[i].size(); k++)
                if(combinedEvents[i].get(k).getTeam2() != null)
                    names[i].add(combinedEvents[i].get(k).getTeam1() + " vs " + combinedEvents[i].get(k).getTeam2());
                else
                    names[i].add(combinedEvents[i].get(k).getTeam1());

            lists[i].setItems(FXCollections.observableArrayList(names[i]));
        }
    }

    private void writeToFile(Object o, String n) throws IOException{
        File f = new File(n+".dat");
        FileOutputStream fs = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fs);

        os.writeObject(o);
        os.flush();
        os.close();
        fs.flush();
        fs.close();
    }

    private Object readFromFile(String n) throws IOException, ClassNotFoundException{
        File f = new File(n+".dat");
        FileInputStream fi = new FileInputStream(f);
        ObjectInputStream oi = new ObjectInputStream(fi);

        return oi.readObject();
    }

    private void displayError(String message){
        Stage s = new Stage();
        Text t = new Text(message);
        Button b = new Button("Ok");
        FlowPane p = new FlowPane(t, b);

        b.setOnAction(e->s.close());

        s.setScene(new Scene(p, 200, 100));
        s.setTitle("Error Message!!");
        s.setResizable(false);
        s.show();
    }

    @Override
    public void stop(){
        try {
            writeToFile(savedData, "Data");
        }catch (IOException io){io.printStackTrace();}
        System.exit(0);
    }

}


