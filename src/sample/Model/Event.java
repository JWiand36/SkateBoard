package sample.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * Created by John Wiand on 10/23/2016.
 */
public class Event implements Serializable{

    private int id;
    private String team1;
    private String team2;
    private int locker1;
    private int locker2;
    private LocalDateTime startTime;
    private int rinkNum;

    public Event(String team1, String team2, int locker1, int locker2, LocalDateTime startTime){
        this.team1 = team1;
        this.team2 = team2;
        this.locker1 = locker1;

        if(locker1 < 5){
            rinkNum = 1;
        }else{
            rinkNum = 2;
        }

        this.locker2 = locker2;
        this.startTime = startTime;
    }

    public Event (String team1, int locker1, LocalDateTime startTime){
        this.team1 = team1;
        this.locker1 = locker1;

        if(locker1 < 5){
            rinkNum = 1;
        }else{
            rinkNum = 2;
        }

        this.startTime = startTime;
    }

    public Event(String team1, String team2, LocalDateTime startTime, int rinkNum){
        this.team1 = team1;
        this.team2 = team2;
        this.locker1 = -1;
        this.locker2 = -1;
        this.rinkNum = rinkNum;
        this.startTime = startTime;
    }

    public Event (String team1, LocalDateTime startTime, int rinkNum){
        this.team1 = team1;
        this.locker1 = -1;
        this.rinkNum = rinkNum;
        this.startTime = startTime;
    }

    public String getTeam1() { return team1; }

    public String getTeam2() { return team2; }

    public int getLocker1() { return locker1; }

    public int getLocker2() { return locker2; }

    public LocalDateTime getStartTime() { return startTime; }

    public int getRinkNum() { return rinkNum; }

    public String getAm() {
        if(isAm())
            return "AM";

        return "PM";
    }

    public boolean isAm() {

        int hour = startTime.getHour();

        return hour < 12;
    }

    public int getId() { return id;}

    public void setTeam1(String team1) {this.team1 = team1;}

    public void setTeam2(String team2) {this.team2 = team2;}

    public void setRink(int rink) {this.rinkNum = rink;}

    public void setLocker1(int locker1){ this.locker1 = locker1; }

    public void setLocker2(int locker2){ this.locker2 = locker2; }

    public void setStartTime(LocalDateTime time) { this.startTime = time; }

    public void setId(int id) { this.id = id; }

    public String toString() {
        String result = "Next Event at: ";

        int hour = this.getStartTime().getHour();
        if(hour > 11)
            hour = hour - 12;

        if(this.getStartTime().getMinute() < 10)
            result += "Time: " + hour + " :0" + this.getStartTime().getMinute() + " " + this.getAm() + "\n";
        else
            result += "Time: " + hour + " :" + this.getStartTime().getMinute() + " " + this.getAm() + "\n";

        result += this.getTeam1() + " - Locker:" + this.getLocker1() + "  /  ";

        if (this.getTeam2() != null) {
            result += this.getTeam2() + " - Locker:" + this.getLocker2();
        }

        return result;
    }
}
