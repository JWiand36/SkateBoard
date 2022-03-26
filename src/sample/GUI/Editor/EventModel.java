package sample.GUI.Editor;

import sample.Model.Event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Locale;

public class EventModel implements Serializable {

    private int id;
    private String team1;
    private String team2;
    private String locker1;
    private String locker2;
    private String startTime;
    private String lastDay;
    private String rinkNum;

    private final LocalDateTime startTimeCal;

    public EventModel(Event event){
        this.team1 = event.getTeam1();
        this.team2 = event.getTeam2();
        this.locker1 = Integer.toString(event.getLocker1());
        this.locker2 = Integer.toString(event.getLocker2());
        this.rinkNum = Integer.toString(event.getRinkNum());
        this.id = event.getId();

        this.startTimeCal = event.getStartTime();

        Month month = startTimeCal.getMonth();
        int day = startTimeCal.getDayOfMonth();

        this.lastDay = Integer.toString(month.getValue()) + "/" + Integer.toString(day);

        LocalTime hour = startTimeCal.toLocalTime();
        this.startTime = hour.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.US));

    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public String getLocker1() {
        return locker1;
    }

    public String getLocker2() {
        return locker2;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getLastDay() {
        return lastDay;
    }

    public String getRinkNum() { return rinkNum; }

    public void setTeam1(String team1) {this.team1 = team1;}

    public void setTeam2(String team2) {this.team2 = team2;}

    public void setRink(String rink) {this.rinkNum = rink;}

    public void setLocker1(String locker1){ this.locker1 = locker1; }

    public void setLocker2(String locker2){ this.locker2 = locker2; }

    public void setStartTime(String time) {this.startTime = time;}

    public void setLastDay(String time) { this.lastDay = time;}

    public Event returnEvent(){
        Event event = new Event(getTeam1(), getTeam2(), Integer.parseInt(getLocker1()), Integer.parseInt(getLocker2()), startTimeCal);
        event.setId(id);
        return event;
    }
}
