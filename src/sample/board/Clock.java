package sample.board;

import javafx.application.Platform;

import java.util.Calendar;

public class Clock implements Runnable{

    private Calendar calendar = Calendar.getInstance();
    private int stop = 1;
    private int second;
    private int min;
    private int hour;
    private int day_of_month;
    private int dayNight;
    private int week_day_number;
    private int savedDay = calendar.get(Calendar.DAY_OF_MONTH);
    private String dayNightCycle;
    private String[] weekDayString = {"Sunday","Monday","Tuesday", "Wednesday", "Thursday", "Friday","Saturday"};
    private Main main;


    Clock(Main main){
        this.main = main;
    }

    @Override
    public void run(){
        System.out.println("Running Clock");
        try {

            while (stop == 1) {
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
                    this.nextEvent();
                }

                //Resets the day and manipulates the GridPane to display the correct information.
                if(savedDay != day_of_month){
                    this.nextDay();
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

                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {ex.printStackTrace(); new Thread(new Clock(main));
        }
    }

    public void stop(){
        stop = 0;
    }

    private void nextEvent(){
        main.changeEvent(hour, min, dayNightCycle, week_day_number);
    }

    private void nextDay(){
        main.changeDay(week_day_number);
    }

    public int getDay() {
        System.out.println("Getting New Day " + week_day_number);
        return week_day_number;
    }
}

