package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class Main extends Application {

    private int today;
    private int currentEventNbr1;
    private int currentEventNbr2;

    private ArrayList<Event>[] events = new ArrayList[14];
    private Text clock;
    private Text messageTxt;
    private Text specialMessage;
    private BorderPane mainBorder;
    private BorderPane innerBorder;
    private BorderPane upperBorder;
    private FlowPane specialMessagePane;
    private FlowPane rink1CheckPane;
    private FlowPane rink2CheckPane;
    private File[] files;
    private Pane imagePane;
    private Pane messagePane;
    private PathTransition messagePath;
    private ImageView logo;

    @Override
    public void start(Stage primaryStage) {

        innerBorder = new BorderPane();
        upperBorder = new BorderPane();
        FlowPane clockPane = new FlowPane();
        specialMessagePane = new FlowPane();
        messagePane = new Pane();
        mainBorder = new BorderPane();
        imagePane = new Pane();

        for (int i = 0; i < events.length; i++)
            events[i] = new ArrayList<>();

        try{
            events = (ArrayList<Event>[]) readFromFile("Events");
        }catch (IOException io){}
        catch (ClassNotFoundException not){}

        clock = new Text();
        specialMessage = new Text("");
        messageTxt = new Text("");

        try {
            logo = new ImageView(readImageFile("logo"));

            mainBorder.setBackground(new Background(new BackgroundImage(readImageFile("Ice"),
                    BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,
                    new BackgroundSize(mainBorder.getWidth(),mainBorder.getHeight(),false,false,false,true))));
        }catch (IOException io){io.printStackTrace();}

        logo.setFitHeight(75);
        logo.setFitWidth(75);

        clock.setFont(new Font(20));

        specialMessage.setStyle("-fx-font-weight: bold; " +
                "-fx-fill: white;");

        messageTxt.setFont(new Font((18)));

        imagePane.setPrefHeight(150);

        messagePane.getChildren().add(messageTxt);
        messagePane.setMinHeight(40);

        clockPane.getChildren().addAll(logo,clock);
        clockPane.setAlignment(Pos.CENTER);

        specialMessagePane.setStyle("-fx-background-color: #cc0000");
        specialMessagePane.getChildren().add(specialMessage);
        specialMessagePane.setAlignment(Pos.TOP_CENTER);
        specialMessagePane.setPrefHeight(40);

        innerBorder.setBottom(messagePane);
        innerBorder.setCenter(imagePane);

        upperBorder.setCenter(clockPane);

        mainBorder.setBottom(innerBorder);
        mainBorder.setTop(upperBorder);
        mainBorder.setPadding(new Insets(5,5,0,5));

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Scene scene = new Scene(mainBorder, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());

        //The Clock of the program
        new Thread(new RunClock()).start();

        //Networking
        new Thread(new RunServer()).start();

        runMessage("");

        displayNewDay(today);

        try {
            File dir = new File("Pictures");
            files = dir.listFiles();

            runImage();
        }catch (IOException nul){nul.printStackTrace();}

        primaryStage.setTitle("");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*An If Nightmare, tread carefully, checks the time for the next event and continues if the event has already passed
      Still needs to be thoroughly tested.
    */
    private Text checkTimes(int currentHour, int currentMin, String dayNightCycle, ArrayList<Event> dayList, int counter){

        Text nxtEvntRink = new Text();

        try {
            Event currentEvent1 = dayList.get(counter);

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

            nxtEvntRink = new Text(dayList.get(counter).toString());
            nxtEvntRink.setStyle("-fx-font-size: 16;");

            if(dayList.get(0).equals(events[today].get(0))) {
                if (counter != currentEventNbr1)
                    currentEventNbr1 = (currentEventNbr1 + 1) % dayList.size();
            }else
                if(counter != currentEventNbr2)
                    currentEventNbr2 = (currentEventNbr2 + 1) % dayList.size();

            return nxtEvntRink;

        }catch (NullPointerException np){System.out.println("No Event Found - NP");}catch (IndexOutOfBoundsException iob){
            System.out.println("No Event Found - IOB");
        }

        return nxtEvntRink;
    }

    private void displayNewDay(int today){

        Platform.runLater(()->{
            try {

                GridPane rink1 = new GridPane();
                GridPane rink2 = new GridPane();

                mainBorder.setLeft(setUpDisplay(rink1, events[today], "Rink 1"));
                mainBorder.setRight(setUpDisplay(rink2, events[today+7], "Rink 2"));

            }catch (IndexOutOfBoundsException id){id.printStackTrace();}
            catch (NullPointerException nul){System.out.println("Null Value in displayNewDay");}
        });

    }

    //Sorts the arrays of Events to be in ascending order of time
    private ArrayList<Event>[] sortEvents(ArrayList<Event>[] events){
        ArrayList<Event>[] result = new ArrayList[events.length];
        ArrayList<Event>[] sortArrays = new ArrayList[12];
        ArrayList<Event> am = new ArrayList<>();
        ArrayList<Event> pm = new ArrayList<>();

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
        return result;
    }

    @Override
    public void stop(){
        try {
            writeToFile(events, "Events");
        }catch (IOException io){}
        System.exit(0);
    }

    //The networking of the program.
    private class RunServer implements Runnable{
        @Override
        public void run(){
            try {
                ServerSocket serverSocket = new ServerSocket(36);

                while(true) {

                    //Looks for the communication of the client, the Input/Output Stream must be in order
                    Socket socket = serverSocket.accept();
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());


                    byte selection = input.readByte();

                    //Sends the data
                    if(selection == 1){
                        objOutput.writeObject(events);
                        objOutput.flush();
                        objOutput.close();
                    }

                    //Receive the data
                    if(selection == 2) {
                        events = (ArrayList<Event>[]) objInput.readObject();
                        events = sortEvents(events);
                        displayNewDay(today);
                        currentEventNbr1 = 0;
                        currentEventNbr2 = 0;
                    }

                    if(selection == 3){
                        specialMessage.setText((String)objInput.readObject());
                        specialMessage.setFont(new Font(20));

                        Platform.runLater(()->innerBorder.setTop(specialMessagePane));
                    }

                    if(selection == 4){
                        Platform.runLater(()->innerBorder.setTop(null));
                    }

                    if(selection == 5){
                        runMessage((String)objInput.readObject());
                    }

                    socket.close();
                }
            }catch (IOException io){io.printStackTrace(); new Thread(new RunServer()).start();}
            catch (ClassNotFoundException cnf){cnf.printStackTrace(); new Thread(new RunServer()).start();}


        }
    }

    //The Clock of the program
    private class RunClock implements Runnable{

        @Override
        public void run(){
            try {
                Calendar calendar = Calendar.getInstance();
                int second;
                int min;
                int hour;
                int day;
                int dayNight;
                int savedDay = calendar.get(Calendar.DAY_OF_MONTH);
                int weekDayNumber;
                String dayNightCycle;
                String[] weekDay = {"Sunday","Monday","Tuesday", "Wednesday", "Thursday", "Friday","Saturday"};

                while (true) {
                    //Checks the system clock for the time
                    calendar = Calendar.getInstance();
                    second = calendar.get(Calendar.SECOND);
                    min = calendar.get(Calendar.MINUTE);
                    hour = calendar.get(Calendar.HOUR);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    dayNight = calendar.get(Calendar.AM_PM);
                    weekDayNumber = (calendar.get(Calendar.DAY_OF_WEEK)-1)%7;

                    today = weekDayNumber;


                    if(hour == 0)
                        hour = 12;

                    if(dayNight == 1)
                        dayNightCycle = "pm";
                    else
                        dayNightCycle = "am";

                    //Checks the time to see if it's 10 min after the current event
                    if(second%60 == 0) {
                        rink1CheckPane = new FlowPane(checkTimes(hour, min, dayNightCycle, events[today], currentEventNbr1));
                        rink2CheckPane = new FlowPane(checkTimes(hour, min, dayNightCycle, events[today+7], currentEventNbr2));

                        rink1CheckPane.setAlignment(Pos.CENTER);
                        rink2CheckPane.setAlignment(Pos.CENTER);

                        Platform.runLater(()->{
                            upperBorder.setLeft(rink1CheckPane);
                            upperBorder.setRight(rink2CheckPane);
                        });
                    }

                    if(savedDay != day){
                        savedDay = day;
                        events[(today+6)%7].clear();
                        displayNewDay(today);
                        currentEventNbr1 = 0;
                        currentEventNbr2 = 0;

                        Platform.runLater(()->innerBorder.setTop(null));
                    }

                    String time;

                    if(min < 10)
                        if(second < 10)
                            time = weekDay[weekDayNumber] + " " + day + ", " + hour + ":0"+ min + ".0" + second + " "+ dayNightCycle;
                        else
                            time = weekDay[weekDayNumber] + " " + day + ", " + hour + ":0"+ min + "." + second + " "+ dayNightCycle;
                    else
                        if(second < 10)
                            time = weekDay[weekDayNumber] + " " + day + ", " + hour + ":" + min + ".0" + second + " "+ dayNightCycle;
                        else
                            time = weekDay[weekDayNumber] + " " + day + ", " + hour + ":"+ min + "." + second + " "+ dayNightCycle;

                    Platform.runLater(() ->clock.setText(time));

                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {ex.printStackTrace(); new Thread(new RunClock());
            }
        }
    }

    //Image work at the bottom of the program
    private void runImage() throws IOException{

        int amountOfPics = 8;

        Timeline timeline = new Timeline();
        ImageView[] pictures = new ImageView[amountOfPics];
        int x = 0;

        for(int i = 0; i < amountOfPics; i++) {
            pictures[i] = new ImageView(getImage());
            pictures[i].setFitWidth(275);
            pictures[i].setFitHeight(150);
            imagePane.getChildren().add(pictures[i]);
        }

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
    private Image getImage() throws IOException {

        int index = (int) (Math.random() * files.length);

        File f = new File(files[index].toString());
        FileInputStream fi = new FileInputStream(f);
        Image i = new Image(fi);

        return i;
    }

    private void runMessage(String message){
        messagePath = new PathTransition();
        Line line = new Line(message.length()+1600,25,-300-message.length(),25);

        messagePath.setDuration(Duration.millis(40000));
        messagePath.setCycleCount(Timeline.INDEFINITE);
        messagePath.setNode(messageTxt);
        messagePath.setPath(line);
        messagePath.play();
        messageTxt.setText(message);
    }

    private GridPane setUpDisplay(GridPane pane, ArrayList<Event> day, String rink){

        ColumnConstraints col1 = new ColumnConstraints(85);
        ColumnConstraints col2 = new ColumnConstraints(150);
        ColumnConstraints col3 = new ColumnConstraints(85);
        ColumnConstraints col4 = new ColumnConstraints(150);
        ColumnConstraints col5 = new ColumnConstraints(85);
        RowConstraints row0 = new RowConstraints(30);
        RowConstraints row1 = new RowConstraints(20);

        String[] text = {"Time", "Team 1", "Locker", "Team 2", "Locker"};

        pane.getColumnConstraints().addAll(col1,col2,col3,col4,col5);
        pane.getRowConstraints().addAll(row0,row1);

        pane.setStyle("-fx-font-size: 25");
        pane.setHgap(15);

        Text time;
        Text rink1t = new Text(rink);
        rink1t.setStyle("-fx-font-size: 30;");
        pane.add(rink1t,2,0);

        pane.add(new Text(text[0]),0,1);
        pane.add(new Text(text[1]),1,1);
        pane.add(new Text(text[2]),2,1);
        pane.add(new Text(text[3]),3,1);
        pane.add(new Text(text[4]),4,1);

        for(int i = 0; i < day.size(); i++) {
            Text team1 = new Text(day.get(i).getTeam1());
            team1.setStyle("-fx-font-size: 18;");
            pane.add(team1,1,i+2);

            if(day.get(i).getStartMin() < 10)
                time = new Text(day.get(i).getStartHour()+":0"+day.get(i).getStartMin()+
                        "  "+day.get(i).getDayNightCycle());
            else
                time = new Text(day.get(i).getStartHour()+":"+day.get(i).getStartMin()+
                    "  "+day.get(i).getDayNightCycle());

            time.setStyle("-fx-font-size: 18;");
            pane.add(time,0,i+2);

            Text locker1 = new Text(day.get(i).getLocker1()+"");
            locker1.setStyle("-fx-font-size: 18;");
            pane.add(locker1,2,i+2);

            if(day.get(i).getTeam2() != null){
                Text team2 = new Text(day.get(i).getTeam2());
                team2.setStyle("-fx-font-size: 18;");
                pane.add(team2,3,i+2);

                Text locker2 = new Text(day.get(i).getLocker2()+"");
                locker2.setStyle("-fx-font-size: 18;");
                pane.add(locker2,4,i+2);

            }

        }

        return pane;
    }

    private void writeToFile(Object objectToFile, String nameOfFile) throws IOException{
        File f = new File(nameOfFile+".dat");
        FileOutputStream fs = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fs);

        os.writeObject(objectToFile);
        os.flush();
        os.close();
        fs.flush();
        fs.close();
    }

    private Object readFromFile(String nameOfFile) throws IOException, ClassNotFoundException{
        File f = new File(nameOfFile+".dat");
        FileInputStream fi = new FileInputStream(f);
        ObjectInputStream oi = new ObjectInputStream(fi);

        return oi.readObject();
    }

    private Image readImageFile(String nameOfFile) throws IOException{
        File f = new File(nameOfFile+".JPG");
        FileInputStream fi = new FileInputStream(f);

        return new Image(fi);
    }

    public static void main(String[] args) {
        launch(args);
    }
}