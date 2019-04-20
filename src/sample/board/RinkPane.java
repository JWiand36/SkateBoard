package sample.board;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import sample.Event;

import java.util.ArrayList;

class RinkPane extends GridPane {

    /*The nextEventNbr helps the program discover the next event. It holds the value of next event until the time
      pass the time of the event.
      savedEventNbr is used to work with the styling of FlowPanes
    */
    private int nextEventNbr;
    private int savedEventNbr;
    private int fontSize;

    private Text[] text = {new Text(""), new Text("Time"), new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker")};

    RinkPane(int fontSize, int rinkNbr){
        setUpDisplay(fontSize);
        this.fontSize = fontSize;
        text[0].setText(("Rink " + rinkNbr));
    }

    void changeEvent(int hour, int min, String dayNightCycle, ArrayList<Event> dayList){
        checkTimes(hour, min, dayNightCycle, dayList);

        /*If the time surpasses the next events time. This will change the Style of the desired
         FlowPanes and save the new event.
        */
        if(savedEventNbr != nextEventNbr) {
            //The IDE complains but, it's set this so both Rinks can change simultaneously.
            Platform.runLater(() -> this.changeCurrentEvent());
            savedEventNbr = nextEventNbr;
        }
    }

    //Changes the settings of the GridPane, it is used to set up the Pane and supposed to be left alone if data is manipulated
    private void setUpDisplay(int fontSize) {

        ColumnConstraints[] col = new ColumnConstraints[5];

        for(int i = 0; i < col.length; i+=2){
            col[i] = new ColumnConstraints((4*fontSize));
            if(i < 4)
                col[i+1] = new ColumnConstraints((7*fontSize));
        }

        this.getColumnConstraints().addAll(col[0],col[1],col[2],col[3],col[4]);

        this.setStyle("-fx-font-size: "+fontSize+";");

        this.add(text[0], 2, 0);

        this.add(text[1], 0, 1);
        this.add(text[2], 1, 1);
        this.add(text[3], 2, 1);
        this.add(text[4], 3, 1);
        this.add(text[5], 4, 1);
    }

    private void changeCurrentEvent(){

        /*This styles the FlowPanes of the desired cells in the GridPane. It will change to the next event to blue and
          the last event to clear.
         */
        for(Node node : this.getChildren()){
            if (GridPane.getRowIndex(node) == nextEventNbr + 2)
                node.setStyle("-fx-background-color: #99C0E6;");
            else
                node.setStyle("-fx-background-color: null;");
        }
    }

    /*
    An If Nightmare, tread carefully, checks the time for the next event and continues if the event has already passed
    */
    private void checkTimes(int currentHour, int currentMin, String dayNightCycle, ArrayList<Event> dayList){

        try {
            sample.Event currentEvent1 = dayList.get(nextEventNbr);

            //If the current hour doesn't equal the start time of the event
            if(currentHour != currentEvent1.getStartHour()) {

                if (currentEvent1.getDayNightCycle().equals(dayNightCycle)) {
                    if (currentHour % 12 > currentEvent1.getStartHour() % 12)
                        nextEventNbr = (nextEventNbr + 1) % dayList.size();

                }else
                if(dayNightCycle.equals("pm"))
                    if (currentHour % 12 < currentEvent1.getStartHour() % 12 || currentEvent1.getDayNightCycle().equals("am"))
                        nextEventNbr = (nextEventNbr + 1) % dayList.size();
            }

            //Current hour and the event start time equals but the day night cycle are off then it goes to the next event
            if(currentHour == currentEvent1.getStartHour() && !dayNightCycle.equals(currentEvent1.getDayNightCycle())) {
                nextEventNbr = (nextEventNbr + 1) % dayList.size();
            }

            /*If the current hour and the start hour are the same and Day/Night cycle match it checks the Mins,
              if the current times Mins are greater then the current event's start time then is takes the next
              event, else it moves on.
             */
            if(currentHour == currentEvent1.getStartHour() && dayNightCycle.equals(currentEvent1.getDayNightCycle())) {
                if (currentMin > currentEvent1.getStartMin())
                    nextEventNbr = (nextEventNbr + 1) % dayList.size();
            }

        }catch (NullPointerException np){
            System.out.println("No Event Found - NP");
            np.printStackTrace();

        }catch (IndexOutOfBoundsException iob){
            System.out.println("No Event Found - IOB");
        }
    }

    //Changes the information that is to be displayed on the GridPane. It styles the information and displays it.
    private void setRinkInfo(ArrayList<Event> day){

        Text time;

        this.getChildren().retainAll(text);


        for(int i = 0; i < day.size(); i++) {
            Text team1 = new Text(day.get(i).getTeam1());
            team1.setStyle("-fx-font-size: "+(fontSize-7)+";");
            this.add(new FlowPane(team1),1,i+2);

            if(day.get(i).getStartMin() < 10)
                time = new Text(day.get(i).getStartHour()+":0"+day.get(i).getStartMin()+
                        "  "+day.get(i).getDayNightCycle());
            else
                time = new Text(day.get(i).getStartHour()+":"+day.get(i).getStartMin()+
                        "  "+day.get(i).getDayNightCycle());

            time.setStyle("-fx-font-size: "+(fontSize-7)+";");
            this.add(new FlowPane(time),0,i+2);

            Text locker1 = new Text(day.get(i).getLocker1()+"");
            locker1.setStyle("-fx-font-size: "+(fontSize-7)+";");
            this.add(new FlowPane(locker1),2,i+2);

            if(day.get(i).getTeam2() != null){
                Text team2 = new Text(day.get(i).getTeam2());
                team2.setStyle("-fx-font-size: "+(fontSize-7)+";");
                this.add(new FlowPane(team2),3,i+2);

                Text locker2 = new Text(day.get(i).getLocker2()+"");
                locker2.setStyle("-fx-font-size: "+(fontSize-7)+";");
                this.add(new FlowPane(locker2),4,i+2);

            }

        }
    }

    private void resetNewDay(int hour, int min, String dayNight, ArrayList<Event> events){

        nextEventNbr = 0;

        for(int i = 0; i < events.size(); i++){
            checkTimes(hour, min, dayNight, events);
        }
    }

    void displayNewDay(int hour, int min, String dayNight, ArrayList<Event> events){

        //This method is used to manipulate the GridPane if the data is changed.
        Platform.runLater(()->{
            try {

                //Builds and fills the GridPane with data
                setRinkInfo(events);

                resetNewDay(hour, min, dayNight, events);

                //Styles the FlowPanes
                changeCurrentEvent();

            }catch (IndexOutOfBoundsException | NullPointerException id){
                id.printStackTrace();
            }
        });

    }
}
