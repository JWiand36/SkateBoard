package sample;

import java.io.Serializable;

/**
 * Created by John Wiand on 10/23/2016.
 */
public class Event implements Serializable{

    private String team1;
    private String team2;
    private int locker1;
    private int locker2;
    private int startHour;
    private int startMin;
    private String dayNightCycle;
    private int rinkNum;

    public Event(String team1, String team2, int locker1, int locker2, int startHour, int startMin, String dayNightCycle){
        this.team1 = team1;
        this.team2 = team2;
        this.locker1 = locker1;

        if(locker1 < 5){
            rinkNum = 1;
        }else{
            rinkNum = 2;
        }

        this.locker2 = locker2;
        this.startHour = startHour;
        this.startMin = startMin;
        this.dayNightCycle = dayNightCycle;
    }

    public Event (String team1, int locker1, int startHour, int startMin, String dayNightCycle){
        this.team1 = team1;
        this.locker1 = locker1;

        if(locker1 < 5){
            rinkNum = 1;
        }else{
            rinkNum = 2;
        }

        this.startHour = startHour;
        this.startMin = startMin;
        this.dayNightCycle = dayNightCycle;
    }

    public Event(String team1, String team2, int startHour, int startMin, String dayNightCycle, int rinkNum){
        this.team1 = team1;
        this.team2 = team2;
        this.locker1 = -1;
        this.locker2 = -1;
        this.rinkNum = rinkNum;
        this.startHour = startHour;
        this.startMin = startMin;
        this.dayNightCycle = dayNightCycle;
    }

    public Event (String team1, int startHour, int startMin, String dayNightCycle, int rinkNum){
        this.team1 = team1;
        this.locker1 = -1;
        this.rinkNum = rinkNum;
        this.startHour = startHour;
        this.startMin = startMin;
        this.dayNightCycle = dayNightCycle;
    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public int getLocker1() {
        return locker1;
    }

    public int getLocker2() {
        return locker2;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public int getRinkNum() { return rinkNum; }

    public String getDayNightCycle() {
        return dayNightCycle;
    }

    public void setLocker1(int locker1){ this.locker1 = locker1; }

    public void setLocker2(int locker2){ this.locker2 = locker2; }

    public String toString() {
        String result = "Next Event at: ";

        if (this.getStartMin() < 10)
            result += "Time: " + this.getStartHour() + " :0" + this.getStartMin() + " " + this.getDayNightCycle() + "\n";
        else
            result += "Time: " + this.getStartHour() + " :" + this.getStartMin() + " " + this.getDayNightCycle() + "\n";

        result += this.getTeam1() + " - Locker:" + this.getLocker1() + "  /  ";

        if (this.getTeam2() != null) {
            result += this.getTeam2() + " - Locker:" + this.getLocker2();
        }

        return result;
    }
}
