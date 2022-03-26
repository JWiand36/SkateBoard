package sample;

import javafx.stage.Stage;
import sample.Model.Event;
import sample.Model.EventCollection;
import sample.Services.Clock;
import sample.Services.FileIO;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;


public class Controller {

    private final Clock clock = new Clock(this);
    EventEditor editor;
    DisplayBoard board;
    EventCollection collection;
    Stage displayStage;
    Thread clockThread = new Thread(clock);

    public Controller() { collection = new EventCollection(); }

    public void startClock(){ clockThread.start(); }

    public void setEditor(EventEditor editor) { this.editor = editor; }

    public void setBoard(DisplayBoard board) { this.board = board; }

    public void setDisplayStage(Stage displayStage) {this.displayStage = displayStage;}

    public void closeBoard(){ displayStage.close(); }

    public  void showBoard(){ displayStage.show(); }

    public void nextDay(LocalDateTime currentTime) {
        collection.removeExpiredEvents();
        board.changeDay(currentTime, collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 1),
                collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 2));
        editor.changeDay();
    }

    public void changeEvent(LocalDateTime currentTime) {
        board.changeEvent(currentTime, collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 1),
                collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 2));
    }

    public void removeExpiredEvents(){ nextDay(LocalDateTime.now()); }

    public ArrayList<Event> getDay(int weekDayNum) { return collection.getDay(weekDayNum); }

    public void setClock(String time) { board.setClock(time); }

    public void stop() {

        try {
            ArrayList<Event>[] events = collection.getAllEvents();
            FileIO.writeToFile(events, "Events");
        } catch (IOException io) {
            io.getStackTrace();
        }

        clock.stop();
    }

    public void sendMessage(String message) { board.setPromotionalMessage(message); }

    public void sendAlertMessage(String message) {
        board.setAlertMessage(message);
        board.displayAlertMessage(true);
    }

    public void removeAlertMessage() { board.displayAlertMessage(false); }

    public void updateDay() {
        LocalDateTime currentTime = LocalDateTime.now();
        board.updateBoard(currentTime, collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 1),
                collection.getRinkCurrentDay(currentTime.getDayOfWeek().getValue(), 2));
    }

    public void resetCollection (){
        collection.resetCollection();
        editor.changeDay();
        updateDay();
    };

    public void saveEvent(Event modifyingEvent){
        if(collection.doesEventExist(modifyingEvent, editor.getDayOfWeek().getValue()))
            collection.updateEvent(modifyingEvent, editor.getAssignLockers(), editor.getDayOfWeek().getValue());
        else
            collection.addEvent(modifyingEvent, editor.getAssignLockers(), editor.getDayOfWeek().getValue());
        editor.saveEvent();

        updateDay();
    }

    public void editEvent(Event modifyingEvent){ editor.editEvent(collection.findEvent(modifyingEvent, editor.getDayOfWeek().getValue())); }

    public void removeEvent(Event event) {
        collection.removeEvent(event, editor.getDayOfWeek().getValue());
        updateDay();
    }

    public DayOfWeek getWeekDay(){
        if(editor != null)
            return editor.getDayOfWeek();
        return LocalDate.now().getDayOfWeek();
    }
}
