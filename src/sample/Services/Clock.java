package sample.Services;

import javafx.application.Platform;
import sample.Controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Locale;

public class Clock implements Runnable{

    private boolean stop = true;
    private final Controller controller;


    public Clock(Controller controller){
        this.controller = controller;
    }

    @Override
    public void run(){

        LocalDateTime calendar = LocalDateTime.now();

        int second;
        int dayOfMonth;
        int savedDay = calendar.getDayOfMonth();
        int min;
        int hour;
        String dayNightCycle;
        int weekDayNumber;


        String[] weekDayString = {"Sunday","Monday","Tuesday", "Wednesday", "Thursday", "Friday","Saturday"};

        nextDay();

        System.out.println("Running Clock");
        try {

            while (stop) {
                //Checks the system clock for the time
                calendar = LocalDateTime.now();
                second = calendar.getSecond();
                min = calendar.getMinute();
                hour = calendar.getHour();
                dayOfMonth = calendar.getDayOfMonth();
                weekDayNumber = (calendar.getDayOfWeek().getValue())%7;

                dayNightCycle = "AM";

                if(hour > 11)
                    dayNightCycle = "PM";

                if(hour > 12)
                    hour -= 12;

                if(hour == 0)
                    hour = 12;

                //Checks the time to see if the time is after the next event
                if(second%60 == 0) {
                    this.nextEvent();
                }

                //Resets the day and manipulates the GridPane to display the correct information.
                if(savedDay != dayOfMonth){
                    this.nextDay();
                    savedDay = dayOfMonth;
                }

                //Sets up the time that is displayed at the top, it's a series of If statements to add Zeros if Min
                //or Sec is less then 10
                String time;

                if(min < 10)
                    if(second < 10)
                        time = weekDayString[weekDayNumber] + " " + dayOfMonth + ", " + hour + ":0"+ min + ".0" + second + " "+ dayNightCycle;
                    else
                        time = weekDayString[weekDayNumber] + " " + dayOfMonth + ", " + hour + ":0"+ min + "." + second + " "+ dayNightCycle;
                else
                if(second < 10)
                    time = weekDayString[weekDayNumber] + " " + dayOfMonth + ", " + hour + ":" + min + ".0" + second + " "+ dayNightCycle;
                else
                    time = weekDayString[weekDayNumber] + " " + dayOfMonth + ", " + hour + ":"+ min + "." + second + " "+ dayNightCycle;

                Platform.runLater(() -> controller.setClock(time));

                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            stop = false;
            ex.printStackTrace();

            System.out.println("Running New Clock");
            new Thread(new Clock(controller));
        }
    }

    public void stop(){
        stop = false;
    }

    private void nextEvent(){ controller.changeEvent(LocalDateTime.now()); }

    private void nextDay(){ controller.nextDay(LocalDateTime.now()); }
}

