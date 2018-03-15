package sample.board;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.Event;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.ArrayList;

public class Main extends Application {

    /*The nextEventNbr 1 & 2 helps the program discover the next event. It holds the value of next event until the time
      pass the time of the event.
      savedEventNbr 1 & 2 is used to work with the styling of FlowPanes
    */
    private int nextEventNbr1;
    private int nextEventNbr2;
    private int savedEventNbr1;
    private int savedEventNbr2;

    private ArrayList<sample.Event>[] events = new ArrayList[14];
    private Text clockText;
    private Text messageTxt;
    private Text specialMessage;
    private Text[] text = {new Text("Time"), new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker"), new Text("Rink 1"), new Text("Time"), new Text("Team 1"), new Text("Locker"), new Text("Team 2"), new Text("Locker"), new Text("Rink 2")};
    private BorderPane innerBorder;
    private FlowPane specialMessagePane;
    private GridPane rink1 = new GridPane();
    private GridPane rink2 = new GridPane();
    private File[] files;
    private Pane imagePane;
    private ImageView logo;
    private Server server;
    private Clock clock;

    @Override
    public void start(Stage primaryStage) {

        innerBorder = new BorderPane();
        BorderPane upperBorder = new BorderPane();
        FlowPane clockPane = new FlowPane();
        specialMessagePane = new FlowPane();
        Pane messagePane = new Pane();
        BorderPane mainBorder = new BorderPane();
        imagePane = new Pane();

        for (int i = 0; i < events.length; i++)
            events[i] = new ArrayList<>();

        try{
            events = (ArrayList<sample.Event>[]) readFromFile("Events");
        }catch (IOException | ClassNotFoundException e){e.getStackTrace();}

        clockText = new Text();
        specialMessage = new Text("");
        specialMessage.setFont(new Font(20));
        messageTxt = new Text("");

        try {
            logo = new ImageView(readImageFile("logo"));

            mainBorder.setBackground(new Background(new BackgroundImage(readImageFile("Ice"),
                    BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,
                    new BackgroundSize(mainBorder.getWidth(),mainBorder.getHeight(),false,false,false,true))));
        }catch (IOException io){io.printStackTrace();}

        logo.setFitHeight(75);
        logo.setFitWidth(75);

        clockText.setFont(new Font(20));

        specialMessage.setStyle("-fx-font-weight: bold; " +
                "-fx-fill: white;");

        messageTxt.setFont(new Font((18)));

        imagePane.setPrefHeight(150);

        messagePane.getChildren().add(messageTxt);
        messagePane.setMinHeight(40);

        clockPane.getChildren().addAll(logo,clockText);
        clockPane.setAlignment(Pos.CENTER);

        specialMessagePane.setStyle("-fx-background-color: #cc0000;" +
                "-fx-alignment: top-center;" +
                "-fx-pref-height: 40");
        specialMessagePane.getChildren().add(specialMessage);

        innerBorder.setBottom(messagePane);
        innerBorder.setCenter(imagePane);

        upperBorder.setCenter(clockPane);

        mainBorder.setBottom(innerBorder);
        mainBorder.setLeft(rink1);
        mainBorder.setRight(rink2);
        mainBorder.setTop(upperBorder);
        mainBorder.setPadding(new Insets(5,5,0,5));

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Scene scene = new Scene(mainBorder, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());

        new Thread(clock = new Clock(this)).start();
        new Thread(server = new Server(this, clock)).start();

        runMessage("");

        rink1 = setUpDisplay(rink1, 0);
        rink2 = setUpDisplay(rink2, 6);

        displayNewDay(clock.getDay());

        try {
            File dir = new File("Pictures");
            files = dir.listFiles();

            runImage();
        }catch (IOException |ArrayIndexOutOfBoundsException nul){nul.printStackTrace();}

        primaryStage.setTitle("");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
    An If Nightmare, tread carefully, checks the time for the next event and continues if the event has already passed
    */
    private int checkTimes(int currentHour, int currentMin, String dayNightCycle, ArrayList<sample.Event> dayList, int counter){

        try {
            sample.Event currentEvent1 = dayList.get(counter);

            //If the current hour doesn't equal the start time of the event
            if(currentHour != currentEvent1.getStartHour()) {

                if (currentEvent1.getDayNightCycle().equals(dayNightCycle)) {
                    if (currentHour % 12 > currentEvent1.getStartHour() % 12)
                        counter = (counter + 1) % dayList.size();

                }else
                if(dayNightCycle.equals("pm"))
                    if (currentHour % 12 < currentEvent1.getStartHour() % 12 || currentEvent1.getDayNightCycle().equals("am"))
                        counter = (counter + 1) % dayList.size();
            }

            //Current hour and the event start time equals but the day night cycle are off then it goes to the next event
            if(currentHour == currentEvent1.getStartHour() && !dayNightCycle.equals(currentEvent1.getDayNightCycle())) {
                counter = (counter + 1) % dayList.size();
            }

            /*If the current hour and the start hour are the same and Day/Night cycle match it checks the Mins,
              if the current times Mins are greater then the current event's start time then is takes the next
              event, else it moves on.
             */
            if(currentHour == currentEvent1.getStartHour() && dayNightCycle.equals(currentEvent1.getDayNightCycle())) {
                if (currentMin > currentEvent1.getStartMin())
                    counter = (counter + 1) % dayList.size();
            }

            return counter;

        }catch (NullPointerException np){System.out.println("No Event Found - NP");}catch (IndexOutOfBoundsException iob){
            System.out.println("No Event Found - IOB");
        }

        return counter;
    }

    private GridPane changeCurrentEvent(int currentEvent, GridPane pane){

        /*This styles the FlowPanes of the desired cells in the GridPane. It will change to the next event to blue and
          the last event to clear.
         */
        for(Node node : pane.getChildren()){
            if (GridPane.getRowIndex(node) == currentEvent + 2)
                node.setStyle("-fx-background-color: #99C0E6;");
            else
                node.setStyle("-fx-background-color: null;");

        }

        return pane;
    }

    void displayNewDay(int today){

        System.out.println("Displaying new Day" + today);
        //This method is used to manipulate the GridPane if the data is changed.
        Platform.runLater(()->{
            try {

                //Builds and fills the GridPane with data
                rink1 = setRinkInfo(rink1, events[today]);
                rink2 = setRinkInfo(rink2, events[today + 7]);

                //Styles the FlowPanes
                rink1 = changeCurrentEvent(nextEventNbr1, rink1);
                rink2 = changeCurrentEvent(nextEventNbr2, rink2);

                savedEventNbr1 = nextEventNbr1;
                savedEventNbr2 = nextEventNbr2;


            }catch (IndexOutOfBoundsException id){id.printStackTrace();}
            catch (NullPointerException nul){System.out.println("Null Value in displayNewDay");}
        });

    }

    //Sorts the arrays of Events to be in ascending order of time, it uses the merge sorting technique.
    void sortEvents(ArrayList<sample.Event>[] events){
        ArrayList<sample.Event>[] result = new ArrayList[events.length];
        ArrayList<sample.Event>[] sortArrays = new ArrayList[12];
        ArrayList<sample.Event> am = new ArrayList<>();
        ArrayList<sample.Event> pm = new ArrayList<>();

        //sets up the result and the array used to sort the data
        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < sortArrays.length; i++)
            sortArrays[i] = new ArrayList<>();

        for(int i = 0; i < result.length; i++){

            //Splits the data from Day/Night Cycle, AM will be completed first
            for(int k = 0; k < events[i].size(); k++){
                if(events[i].get(k).getDayNightCycle().equals("am"))
                    am.add(events[i].get(k));
                else
                    pm.add(events[i].get(k));

            }

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < am.size(); k++)
                sortArrays[am.get(k).getStartHour()%12].add(am.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int selection = 0;
                while(sortArrays[k].size() > 1){
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(selection).getStartMin() > sortArrays[k].get(c).getStartMin())
                            selection += 1;
                        c++;
                    }
                    result[i].add(sortArrays[k].get(selection));
                    sortArrays[k].remove(selection);
                    selection = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            am.clear();

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < pm.size(); k++)
                sortArrays[pm.get(k).getStartHour()%12].add(pm.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int selection = 0;
                while(sortArrays[k].size() > 1){
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(selection).getStartMin() > sortArrays[k].get(c).getStartMin())
                            selection += 1;
                        c++;
                    }
                    result[i].add(sortArrays[k].get(selection));
                    sortArrays[k].remove(selection);
                    selection = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            pm.clear();
        }
        this.events = result;
        displayNewDay(clock.getDay());
        setNextEvent(0,0);
    }

    @Override
    public void stop(){
        try {
            writeToFile(events, "Events");
        }catch (IOException io){io.getStackTrace();}
        clock.stop();
        server.stop();
        System.exit(0);
    }

    //The networking of the program.

    //Image work at the bottom of the program
    private void runImage() throws IOException{

        //The program crashes if there are more then 8 pictures at a time. I don't know why
        int amountOfPics = 8;

        Timeline timeline = new Timeline();
        ImageView[] pictures = new ImageView[amountOfPics];
        int x = 0;

        //Sets up the display settings of the pictures
        for(int i = 0; i < amountOfPics; i++) {
            pictures[i] = new ImageView(getImage());
            pictures[i].setFitWidth(275);
            pictures[i].setFitHeight(150);
            imagePane.getChildren().add(pictures[i]);
        }

        //Sets up the pictures so they will scroll until a certain point which they will be off screen and the pictures
        // will reset
        for( ImageView pic: pictures){
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,new KeyValue(pic.translateXProperty(),x + 1500)),
                    new KeyFrame(Duration.millis(200000), e-> {
                        try {
                            pic.setImage(getImage());
                        } catch (IOException io) {io.printStackTrace();}
                    }, new KeyValue(pic.translateXProperty(), x - 300 * amountOfPics))
            );
            x += 300;
        }

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    //Collects a Random Image from the Pictures folder in the project
    private Image getImage() throws IOException, ArrayIndexOutOfBoundsException{

        int index = (int) (Math.random() * files.length);

        if(files.length != 0) {
            File f = new File(files[index].toString());
            FileInputStream fi = new FileInputStream(f);
            return new Image(fi);
        }
        return null;
    }

    //Runs the promotional message displayed at the bottom of the screen
    void runMessage(String message){
        PathTransition messagePath = new PathTransition();
        Line line = new Line(message.length()+1600,25,-300-message.length(),25);

        messagePath.setDuration(Duration.millis(40000));
        messagePath.setCycleCount(Timeline.INDEFINITE);
        messagePath.setNode(messageTxt);
        messagePath.setPath(line);
        messagePath.play();
        messageTxt.setText(message);
    }

    //Changes the settings of the GridPane, it is used to set up the Pane and supposed to be left alone if data is manipulated
    private GridPane setUpDisplay(GridPane pane, int num) {

        ColumnConstraints[] col = new ColumnConstraints[5];

        for(int i = 0; i < col.length; i+=2){
            col[i] = new ColumnConstraints(85);
            if(i < 4)
                col[i+1] = new ColumnConstraints(150);
        }

        RowConstraints row0 = new RowConstraints(30);
        RowConstraints row1 = new RowConstraints(20);

        pane.getColumnConstraints().addAll(col[0],col[1],col[2],col[3],col[4]);
        pane.getRowConstraints().addAll(row0, row1);

        pane.setStyle("-fx-font-size: 25");

        text[num+5].setStyle("-fx-font-size: 30;");
        pane.add(text[num+5], 2, 0);

        pane.add(text[num], 0, 1);
        pane.add(text[num + 1], 1, 1);
        pane.add(text[num + 2], 2, 1);
        pane.add(text[num + 3], 3, 1);
        pane.add(text[num + 4], 4, 1);

        return pane;
    }

    //Changes the information that is to be displayed on the GridPane. It styles the information and displays it.
    private GridPane setRinkInfo(GridPane pane, ArrayList<Event> day){

        Text time;

        pane.getChildren().retainAll(text);


        for(int i = 0; i < day.size(); i++) {
            Text team1 = new Text(day.get(i).getTeam1());
            team1.setStyle("-fx-font-size: 18;");
            pane.add(new FlowPane(team1),1,i+2);

            if(day.get(i).getStartMin() < 10)
                time = new Text(day.get(i).getStartHour()+":0"+day.get(i).getStartMin()+
                        "  "+day.get(i).getDayNightCycle());
            else
                time = new Text(day.get(i).getStartHour()+":"+day.get(i).getStartMin()+
                        "  "+day.get(i).getDayNightCycle());

            time.setStyle("-fx-font-size: 18;");
            pane.add(new FlowPane(time),0,i+2);

            Text locker1 = new Text(day.get(i).getLocker1()+"");
            locker1.setStyle("-fx-font-size: 18;");
            pane.add(new FlowPane(locker1),2,i+2);

            if(day.get(i).getTeam2() != null){
                Text team2 = new Text(day.get(i).getTeam2());
                team2.setStyle("-fx-font-size: 18;");
                pane.add(new FlowPane(team2),3,i+2);

                Text locker2 = new Text(day.get(i).getLocker2()+"");
                locker2.setStyle("-fx-font-size: 18;");
                pane.add(new FlowPane(locker2),4,i+2);

            }

        }

        return pane;
    }

    public static void writeToFile(Object objectToFile, String nameOfFile) throws IOException{
        File f = new File(nameOfFile+".dat");
        FileOutputStream fs = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fs);

        os.writeObject(objectToFile);
        os.flush();
        os.close();
        fs.flush();
        fs.close();
    }

    public static Object readFromFile(String nameOfFile) throws IOException, ClassNotFoundException{
        File f = new File(nameOfFile+".dat");
        FileInputStream fi = new FileInputStream(f);
        ObjectInputStream oi = new ObjectInputStream(fi);

        return oi.readObject();
    }

    //Images need to be set as a JPG otherwise the exception is thrown. You have to reset the board to fix the problem
    private Image readImageFile(String nameOfFile) throws IOException, ArrayIndexOutOfBoundsException{
        File f = new File(nameOfFile+".JPG");
        FileInputStream fi = new FileInputStream(f);

        return new Image(fi);
    }

    void changeDay(int week_day_number){
        displayNewDay(week_day_number);
        if(week_day_number != 0) {
            events[week_day_number - 1].clear();
            events[week_day_number + 6].clear();
        }else{
            events[week_day_number + 6].clear();
            events[week_day_number + 13].clear();
        }
        nextEventNbr1 = 0;
        nextEventNbr2 = 0;

        Platform.runLater(()->innerBorder.setTop(null));
    }

    void changeEvent(int hour, int min, String dayNightCycle, int week_day_number){
        nextEventNbr1 = checkTimes(hour, min, dayNightCycle, events[week_day_number], nextEventNbr1);
        nextEventNbr2 = checkTimes(hour, min, dayNightCycle, events[week_day_number+7], nextEventNbr2);

                        /*If the time surpasses the next events time. This will changed they Style of the desired
                          FlowPanes and save the new event.
                        */
        if(savedEventNbr1 != nextEventNbr1 || savedEventNbr2 != nextEventNbr2) {
            Platform.runLater(() -> {
                rink1 = changeCurrentEvent(nextEventNbr1, rink1);
                rink2 = changeCurrentEvent(nextEventNbr2, rink2);
            });
            savedEventNbr1 = nextEventNbr1;
            savedEventNbr2 = nextEventNbr2;
        }
    }

    void setClock(String time){
        this.clockText.setText(time);
    }

    public static void main(String[] args) {
        launch(args);
    }

    void setNextEvent(int nextEventNbr1, int nextEventNbr2){
        this.nextEventNbr1 = nextEventNbr1;
        this.nextEventNbr2 = nextEventNbr2;
    }

    void setSpecialMessage(String text){
        specialMessage.setText(text);
    }

    void displaySpecialMessage(boolean display){
        if(display)
            innerBorder.setTop(specialMessagePane);
        else
            innerBorder.setTop(null);
    }

    ArrayList<Event>[] getEvents(){
        return events;
    }
}