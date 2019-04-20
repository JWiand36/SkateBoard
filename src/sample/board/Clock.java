package sample.board;

import javafx.application.Platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

public class Clock implements Runnable{

    private boolean stop = true;
    private boolean ready = false;
    private int week_day_number;
    private Main main;
    private ErrorLogHandler errorLog;


    Clock(Main main, ErrorLogHandler errorLog){
        this.errorLog = errorLog;
        this.main = main;
    }

    @Override
    public void run(){

        Calendar calendar = Calendar.getInstance();

        int second;
        int min;
        int hour;
        int day_of_month;
        int dayNight;
        int savedDay = calendar.get(Calendar.DAY_OF_MONTH);

        String dayNightCycle;

        String[] weekDayString = {"Sunday","Monday","Tuesday", "Wednesday", "Thursday", "Friday","Saturday"};


        System.out.println("Running Clock");
        try {

            while (stop) {
                //Checks the system clock for the time
                calendar = Calendar.getInstance();
                second = calendar.get(Calendar.SECOND);
                min = calendar.get(Calendar.MINUTE);
                hour = calendar.get(Calendar.HOUR);
                day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
                dayNight = calendar.get(Calendar.AM_PM);
                week_day_number = (calendar.get(Calendar.DAY_OF_WEEK)-1)%7;


                if(hour == 0)
                    hour = 12;

                if(dayNight == 1)
                    dayNightCycle = "pm";
                else
                    dayNightCycle = "am";

                //Checks the time to see if the time is after the next event
                if(second%60 == 0) {
                    this.nextEvent(hour, min, dayNightCycle, week_day_number);
                }

                //Resets the day and manipulates the GridPane to display the correct information.
                if(savedDay != day_of_month){
                    this.nextDay(week_day_number);
                    savedDay = day_of_month;
                }

                //Sets up the time that is displayed at the top, it's a series of If statements to add Zeros if Min
                //or Sec is less then 10
                String time;

                if(min < 10)
                    if(second < 10)
                        time = weekDayString[week_day_number] + " " + day_of_month + ", " + hour + ":0"+ min + ".0" + second + " "+ dayNightCycle;
                    else
                        time = weekDayString[week_day_number] + " " + day_of_month + ", " + hour + ":0"+ min + "." + second + " "+ dayNightCycle;
                else
                if(second < 10)
                    time = weekDayString[week_day_number] + " " + day_of_month + ", " + hour + ":" + min + ".0" + second + " "+ dayNightCycle;
                else
                    time = weekDayString[week_day_number] + " " + day_of_month + ", " + hour + ":"+ min + "." + second + " "+ dayNightCycle;

                Platform.runLater(() -> main.setClock(time));

                if(!ready)
                    ready = true;

                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            stop = false;
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            errorLog.processError(sw.toString());

            System.out.println("Running New Clock");
            new Thread(new Clock(main, errorLog));
        }
    }

    void stop(){
        stop = false;
    }

    boolean isReady(){ return ready; }

    private void nextEvent(int hour, int min, String dayNightCycle, int week_day_number){
        main.changeEvent(hour, min, dayNightCycle, week_day_number);
    }

    private void nextDay(int week_day_number){
        main.changeDay(week_day_number);
    }

    int getDay() {
        System.out.println("Getting New Day " + week_day_number);
        return week_day_number;
    }
}

