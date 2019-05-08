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
    private ListView<String> savedInfo = new ListView<>();
    private ListView<String>[] lists;

    private EventCollection eventCollection;
    private NetworkService networkService;

    @Override
    public void start(Stage primaryStage){

        BorderPane mainPane;

        //The eventCollection is init here because it needs lists created which is done in MainPane
        MainPane pane = new MainPane(this);
        eventCollection = new EventCollection(this);
        networkService = new NetworkService(this, eventCollection);

        secondaryStage = new Stage();

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
        private BorderPane fourthPane = new BorderPane();
        private Stage fourthStage = new Stage();
        private Client client;

        //Sets up the view of the MainPane
        private MainPane(Client client) {
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


            fourthStage.setTitle("Modify Info");
            fourthStage.setScene(new Scene(fourthPane,750,660));
            fourthStage.setResizable(false);

        }
        
        private void setModify(int dayNumber){
            fourthStage.close();
            fourthPane.setCenter(new ModifyPane(dayNumber, client, eventCollection));
            fourthStage.show();
        }
    }

    ListView<String> getSavedInfo(){
        return savedInfo;
    }


    //Displays if the user makes a mistake
    void displayError(String message){
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