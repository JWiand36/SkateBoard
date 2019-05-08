package sample.client;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sample.Event;

import java.util.ArrayList;

//This Pane allows the user to manipulate the data when the user selects the desired day
class ModifyPane extends GridPane {

    private ArrayList<String> n;
    private ListView<String> list;
    private Client client;
    private EventCollection eventCollection;

    //Sets up the view of the Modify Pane. It creates 20 rows of TextFields to allow the user to enter in multiple
    //events at once
    ModifyPane(int modifyingDay, Client client, EventCollection eventCollection){
        this.client = client;
        this.eventCollection = eventCollection;

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
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list.");}
        });

        //The user can select data from the list and remove it.
        remove.setOnAction(e->{
            try {
                int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                eventCollection.removeEvent(modifyingDay, selected);
                update(modifyingDay);
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list.");}
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

                ArrayList<Event> savedData = client.getSavedData();

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

                    client.getSavedInfo().setItems(FXCollections.observableArrayList(nameOfData));
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
        client.updateLists(combinedEvents);
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

