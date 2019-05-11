package sample.client;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import sample.Event;

import java.util.ArrayList;

//This Pane allows the user to manipulate the data when the user selects the desired day
class ModifyPane extends GridPane {

    private ArrayList<String> n;
    private ListView<String> list;
    private Client client;

    //Sets up the view of the Modify Pane. It creates 20 rows of TextFields to allow the user to enter in multiple
    //events at once
    ModifyPane(int modifyingDay, Client client){
        this.client = client;

        list = new ListView<>();

        update(modifyingDay, false);

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
        CheckBox automate = new CheckBox("Automate Lockers");

        Text[] t = {new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker"), new Text("Hour"),
                new Text("Minute"), new Text("Am?"), new Text("Rink 1?")};
        TextField[] team1Fields = new TextField[20];
        TextField[] locker1Fields = new TextField[20];
        TextField[] team2Fields = new TextField[20];
        TextField[] locker2Fields = new TextField[20];
        TextField[] hourFields = new TextField[20];
        TextField[] minuteFields = new TextField[20];
        CheckBox[] ampm = new CheckBox[20];
        CheckBox[] rink1 = new CheckBox[20];

        //Checks the info and adds the data if it meets the requirements
        add.setOnAction(e->{
            if(automate.isSelected()) {
                for (int i = 0; i < team1Fields.length; i++)
                    if (checkInfoAdd(team1Fields[i], team2Fields[i], hourFields[i], minuteFields[i], n, ampm[i].isSelected(), rink1[i].isSelected())) {
                        client.addEvent(modifyingDay, modifyInfo(team1Fields[i], team2Fields[i], hourFields[i], minuteFields[i], ampm[i], rink1[i]));
                        update(modifyingDay, automate.isSelected());
                    }
            }else {

                for (int i = 0; i < team1Fields.length; i++)
                    if (checkInfoAdd(team1Fields[i], locker1Fields[i], team2Fields[i], locker2Fields[i], hourFields[i], minuteFields[i], n, ampm[i].isSelected())) {
                        client.addEvent(modifyingDay, modifyInfo(team1Fields[i], locker1Fields[i], team2Fields[i], locker2Fields[i], hourFields[i], minuteFields[i], ampm[i]));
                        update(modifyingDay, automate.isSelected());
                    }
            }
        });

        //The user can select data from the list and modify it. It only uses the top row of TextFields
        modify.setOnAction(e->{
            try {
                if(automate.isSelected()){
                    if (checkInfo(hourFields[0], minuteFields[0])) {
                        int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                        client.removeEvent(modifyingDay, selected);
                        client.addEvent(modifyingDay, modifyInfo(team1Fields[0], team2Fields[0], hourFields[0], minuteFields[0], ampm[0], rink1[0]));
                        update(modifyingDay, automate.isSelected());
                    }
                }else {
                    if (checkInfo(locker1Fields[0], team2Fields[0], locker2Fields[0], hourFields[0], minuteFields[0])) {
                        int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                        client.removeEvent(modifyingDay, selected);
                        client.addEvent(modifyingDay, modifyInfo(team1Fields[0], locker1Fields[0], team2Fields[0], locker2Fields[0], hourFields[0], minuteFields[0], ampm[0]));
                        update(modifyingDay, automate.isSelected());
                    }
                }
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list.");}
        });

        //The user can select data from the list and remove it.
        remove.setOnAction(e->{
            try {
                int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                client.removeEvent(modifyingDay, selected);
                update(modifyingDay, automate.isSelected());
            }catch (ArrayIndexOutOfBoundsException out){client.displayError("Select an Event from the list.");}
        });

        //Sets the look of the text in the ListView
        list.getSelectionModel().selectedItemProperty().addListener(e-> {

            try {
                int selected = n.indexOf(list.getSelectionModel().getSelectedItem());
                Event event = client.getEvent(modifyingDay, selected);
                team1Fields[0].setText(event.getTeam1());
                locker1Fields[0].setText(event.getLocker1() + "");
                team2Fields[0].setText(event.getTeam2());
                locker2Fields[0].setText(event.getLocker2() + "");
                hourFields[0].setText(event.getStartHour() + "");
                minuteFields[0].setText(event.getStartMin() + "");
                if (event.getDayNightCycle().equals("am"))
                    ampm[0].selectedProperty().setValue(true);
                else
                    ampm[0].selectedProperty().setValue(false);

                if (event.getRinkNum() == 1)
                    rink1[0].selectedProperty().setValue(true);
                else
                    rink1[0].selectedProperty().setValue(false);

            }catch (ArrayIndexOutOfBoundsException IOB){IOB.getStackTrace();}
        });

        //Allows the user to store data in the SavedList to be used for another day
        save.setOnAction(e->{
            try {

                ArrayList<Event> savedData = client.getSavedData();

                if(automate.isSelected()){

                    if (checkInfoAdd(team1Fields[0], team2Fields[0], hourFields[0], minuteFields[0], savedData, ampm[0].isSelected(), rink1[0].isSelected())) {
                        savedData.add(modifyInfo(team1Fields[0], team2Fields[0], hourFields[0], minuteFields[0], ampm[0], rink1[0]));
                        ArrayList<String> nameOfData = new ArrayList<>();

                        for (Event event : savedData) {
                            if (event.getTeam2() != null)
                                nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " R:" + event.getRinkNum());
                            else
                                nameOfData.add(event.getTeam1() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " R:" + event.getRinkNum());
                        }

                        client.getSavedInfo().setItems(FXCollections.observableArrayList(nameOfData));
                    }
                }else {

                    if (checkInfoAdd(team1Fields[0], locker1Fields[0], team2Fields[0], locker2Fields[0], hourFields[0], minuteFields[0], savedData, ampm[0].isSelected())) {
                        savedData.add(modifyInfo(team1Fields[0], locker1Fields[0], team2Fields[0], locker2Fields[0], hourFields[0], minuteFields[0], ampm[0]));
                        ArrayList<String> nameOfData = new ArrayList<>();

                        for (Event event : savedData) {
                            if (event.getTeam2() != null)
                                nameOfData.add(event.getTeam1() + " vs " + event.getTeam2() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " R:" + event.getRinkNum());
                            else
                                nameOfData.add(event.getTeam1() +
                                        " " + event.getStartHour() + ":" + event.getStartMin() +
                                        event.getDayNightCycle() + " R:" + event.getRinkNum());
                        }

                        client.getSavedInfo().setItems(FXCollections.observableArrayList(nameOfData));
                    }
                }

            }catch (ArrayIndexOutOfBoundsException out){out.getStackTrace();}
        });

        automate.setOnAction(e->{
            if(automate.isSelected()){
                for(int i = 0; i < locker1Fields.length; i++){
                    this.getChildren().remove(locker1Fields[i]);
                    this.getChildren().remove(locker2Fields[i]);
                    this.add(rink1[i],8, i+1);
                }
                this.getChildren().remove(t[1]);
                this.getChildren().remove(t[3]);
                this.add(t[7], 8, 0);
                col3.setPrefWidth(0);
                col5.setPrefWidth(0);

            }else{
                for(int i = 0; i < rink1.length; i++){
                    this.getChildren().remove(rink1[i]);
                    this.add(locker1Fields[i], 2, i + 1);
                    this.add(locker2Fields[i], 4, i + 1);
                }
                this.add(t[1], 2, 0);
                this.add(t[3], 4, 0);
                this.getChildren().remove(t[7]);
                col3.setPrefWidth(50);
                col5.setPrefWidth(50);

            }
        });

        flow.getChildren().addAll(add,modify,remove, save, automate);
        flow.setHgap(5);

        this.setPadding(new Insets(5));
        this.setHgap(5);
        this.setVgap(5);
        setRowSpan(list, REMAINING);
        setColumnSpan(flow, REMAINING);
        this.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6, col7, col8);

        this.add(list,0,0);

        this.add(flow, 1, team1Fields.length+2);

        for(int i = 1; i < t.length; i++)
            this.add(t[i - 1], i, 0);

        for(int i = 0; i < team1Fields.length; i++){
            team1Fields[i] = new TextField();
            locker1Fields[i] = new TextField();
            team2Fields[i] = new TextField();
            locker2Fields[i] = new TextField();
            hourFields[i] = new TextField();
            minuteFields[i] = new TextField();
            ampm[i] = new CheckBox();
            rink1[i] = new CheckBox();

            this.add(team1Fields[i], 1, i + 1);
            this.add(locker1Fields[i], 2, i + 1);
            this.add(team2Fields[i], 3, i + 1);
            this.add(locker2Fields[i], 4, i + 1);
            this.add(hourFields[i], 5, i + 1);
            this.add(minuteFields[i], 6, i + 1);
            this.add(ampm[i],7,i+1);

        }
    }

    //Checks the data when the user hits the add button. Data can't match other data and the list and it has to meet
    //certain requirements. Team 1 needs to be filled, as Locker Room 1, Hour, Min are all required
    //The data is matched with the ListView's String values to see if the event matches
    private boolean checkInfoAdd(TextField team1, TextField lock1, TextField team2, TextField lock2, TextField hour, TextField min, ArrayList n, boolean ampm){

        //Team1, Locker1, Team2, Locker2, Hour, Min,

        String am;

        //Checks if if their are vales to meet requirements
        if(lock1.getText() != null && hour.getText() != null && min.getText() != null &&
                lock1.getText().length() > 0 && hour.getText().length() > 0 && min.getText().length() > 0&&
                isNumber(lock1.getText()) && isNumber(hour.getText()) && isNumber(min.getText())){

            int l1 = Integer.parseInt(lock1.getText());
            int h = Integer.parseInt(hour.getText());
            int m = Integer.parseInt(min.getText());

            int rink = 2;

            if(l1 < 5 && l1 > 0){
                rink = 1;
            }

            if(ampm)
                am = "am";
            else
                am = "pm";

            //Checks to see if Team 2 and Locker Room 2 are filled, if not the program passes
            if(lock2.getText() != null && lock2.getText().length() > 0 &&
                    team2.getText() != null && team2.getText().length() > 0){
                if(isNumber(lock2.getText())) {
                    int l2 = Integer.parseInt(lock2.getText());

                    if (l2 < 1 || l2 > 8 || n.contains(h+":"+m+am+" "+team1.getText()+" vs "+team2.getText()+
                            " R"+rink))
                        return false;
                }else
                    return false;
            }

            //Checks if the locker Rooms are in range and if the time is correct
            return l1 >= 1 && l1 <= 8 && h >= 0 && h <= 24 && m >= 0 && m <= 60 && !n.contains(h + ":" + m + am + " " + team1.getText() + " R" + rink);
        }else
            return false;
    }

    private boolean checkInfoAdd(TextField team1, TextField team2, TextField hour, TextField min, ArrayList n, boolean ampm, boolean rink1){

        //Checks if if their are vales to meet requirements
        if(hour.getText() != null && min.getText() != null &&
                hour.getText().length() > 0 && min.getText().length() > 0&&
                isNumber(hour.getText()) && isNumber(min.getText())){
            int h = Integer.parseInt(hour.getText());
            int m = Integer.parseInt(min.getText());

            String am = "pm";
            int rink = 2;

            if(rink1){
                rink = 1;
            }

            if(ampm)
                am = "am";

            //Checks to see if Team 2 and Locker Room 2 are filled, if not the program passes
            if(team2.getText() != null && team2.getText().length() > 0){
                if (n.contains(h+":"+m+am+" "+team1.getText()+" vs "+team2.getText()+
                        " R"+rink))
                    return false;
            }

            System.out.println(h >= 0 && h <= 24 && m >= 0 && m <= 60 && !n.contains(h + ":" + m + am + " " + team1.getText() + " R" + rink));
            //Checks if the locker Rooms are in range and if the time is correct
            return h >= 0 && h <= 24 && m >= 0 && m <= 60 && !n.contains(h + ":" + m + am + " " + team1.getText() + " R" + rink);
        }else {
            return false;
        }
    }

    //Same as CheckInfoAdd with minor differences, it doesn't check for Am/Pm. Used for the Modify Button
    private boolean checkInfo(TextField lock1, TextField team2, TextField lock2, TextField hour, TextField min){

        //Team1, Locker1, Team2, Locker2, Hour, Min,

        //Checks to see if Team1, Locker1, Hour and Min, all have values
        if(lock1.getText() != null && hour.getText() != null && min.getText() != null &&
                lock1.getText().length() > 0 && hour.getText().length() > 0 && min.getText().length() > 0&&
                isNumber(lock1.getText()) && isNumber(hour.getText()) && isNumber(min.getText())){

            int l1 = Integer.parseInt(lock1.getText());
            int h = Integer.parseInt(hour.getText());
            int m = Integer.parseInt(min.getText());

            //Checks to see if there is a Team2 or Locker2
            if(lock2.getText() != null && lock2.getText().length() > 0 &&
                    team2.getText() != null && team2.getText().length() > 0){
                if(isNumber(lock2.getText())) {
                    int l2 = Integer.parseInt(lock2.getText());

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

    //Same as CheckInfoAdd with minor differences, it doesn't check for Am/Pm. Used for the Modify Button
    private boolean checkInfo(TextField hour, TextField min){

        //Team1, Locker1, Team2, Locker2, Hour, Min,

        //Checks to see if Team1, Locker1, Hour and Min, all have values
        if(hour.getText() != null && min.getText() != null &&
                hour.getText().length() > 0 && min.getText().length() > 0&&
                isNumber(hour.getText()) && isNumber(min.getText())){

            int h = Integer.parseInt(hour.getText());
            int m = Integer.parseInt(min.getText());

            //Checks Locker1 and time
            return h >= 0 && h <= 24 && m >= 0 && m <= 60;
        }else
            return false;
    }

    //Manipulates the data's values and displays it in the ListView
    private Event modifyInfo(TextField team1F, TextField lock1F, TextField team2F, TextField lock2F, TextField hourF, TextField minF, CheckBox ampm) {

        String team1 = team1F.getText();
        String am;
        int locker1 = Integer.parseInt(lock1F.getText());
        int hour = Integer.parseInt(hourF.getText());
        int min = Integer.parseInt(minF.getText());

        if (ampm.isSelected())
            am = "am";
        else
            am = "pm";

        //Checks if there is a second Team
        if (team2F.getText() != null && lock2F.getText() != null){
            if (team2F.getText().length() > 0 && lock2F.getText().length() > 0) {
                String team2 = team2F.getText();
                int locker2 = Integer.parseInt(lock2F.getText());

                team1F.setText(null);
                lock1F.setText(null);
                team2F.setText(null);
                lock2F.setText(null);
                hourF.setText(null);
                minF.setText(null);

                ampm.selectedProperty().setValue(false);

                hour %= 12;

                if (hour == 0)
                    hour = 12;

                return new Event(team1, team2, locker1, locker2, hour, min, am);
            } else {

                team1F.setText(null);
                lock1F.setText(null);
                team2F.setText(null);
                lock2F.setText(null);
                hourF.setText(null);
                minF.setText(null);

                ampm.selectedProperty().setValue(false);

                hour %= 12;

                if (hour == 0)
                    hour = 12;

                return new Event(team1, locker1, hour, min, am);
            }

            //If there isn't a second event
        }else {

            team1F.setText(null);
            lock1F.setText(null);
            team2F.setText(null);
            lock2F.setText(null);
            hourF.setText(null);
            minF.setText(null);

            ampm.selectedProperty().setValue(false);

            hour %= 12;

            if(hour == 0)
                hour = 12;

            return new Event(team1, locker1, hour, min, am);
        }
    }

    private Event modifyInfo(TextField team1F, TextField team2F, TextField hourF, TextField minF, CheckBox ampm, CheckBox rink1) {

        String team1 = team1F.getText();
        String am = "pm";
        int hour = Integer.parseInt(hourF.getText());
        int min = Integer.parseInt(minF.getText());
        int rink = 2;

        if(rink1.isSelected())
            rink = 1;

        if (ampm.isSelected())
            am = "am";

        //Checks if there is a second Team
        if (team2F.getText() != null){
            if (team2F.getText().length() > 0) {
                String team2 = team2F.getText();

                team1F.setText(null);
                team2F.setText(null);
                hourF.setText(null);
                minF.setText(null);

                ampm.selectedProperty().setValue(false);
                rink1.selectedProperty().setValue(false);

                hour %= 12;

                if (hour == 0)
                    hour = 12;

                return new Event(team1, team2, hour, min, am, rink);
            } else {

                team1F.setText(null);
                team2F.setText(null);
                hourF.setText(null);
                minF.setText(null);

                ampm.selectedProperty().setValue(false);
                rink1.selectedProperty().setValue(false);

                hour %= 12;

                if (hour == 0)
                    hour = 12;

                return new Event(team1, hour, min, am, rink);
            }

            //If there isn't a second event
        }else {

            team1F.setText(null);
            team2F.setText(null);
            hourF.setText(null);
            minF.setText(null);

            ampm.selectedProperty().setValue(false);
            rink1.selectedProperty().setValue(false);

            hour %= 12;

            if(hour == 0)
                hour = 12;

            return new Event(team1, hour, min, am, rink);
        }
    }

    //Used to update the list, sets up the String for the ListView
    private void update(int d, boolean assign){

        n = new ArrayList<>();

        String time;

        client.sortEvents(assign);

        ArrayList<Event>[] combinedEvents = client.getEvents();

        for(int i = 0; i < combinedEvents[d].size(); i++) {

            time = combinedEvents[d].get(i).getStartHour() + ":" + combinedEvents[d].get(i).getStartMin() +
                    combinedEvents[d].get(i).getDayNightCycle() + " ";

            if (combinedEvents[d].get(i).getTeam2() != null)
                n.add(time+combinedEvents[d].get(i).getTeam1() +
                        " vs " + combinedEvents[d].get(i).getTeam2() + " R" + combinedEvents[d].get(i).getRinkNum());
            else
                n.add(time+combinedEvents[d].get(i).getTeam1() + " R" + combinedEvents[d].get(i).getRinkNum());
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

