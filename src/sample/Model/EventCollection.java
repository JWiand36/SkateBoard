package sample.Model;

import sample.Services.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

public class EventCollection {


    private ArrayList<Event>[] events = new ArrayList[7];
    private final EventSorter sorter = new EventSorter();

    public EventCollection(){

        for(int i = 0; i < events.length; i++)
            events[i] = new ArrayList<Event>();

        //Imports the events from file
        try{
            events = (ArrayList<Event>[]) FileIO.readFromFile("Events");

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public ArrayList<Event>[] getAllEvents(){ return events; }

    public ArrayList<Event> getDay(int weekDayNumber) { return events[weekDayNumber - 1]; }

    public ArrayList<Event> getRinkCurrentDay(int weekDayNumber, int rinkNumber) {
        ArrayList<Event>rinkDay = new ArrayList<>();
        for(Event event : getDay(weekDayNumber))
            if(event.getRinkNum() == rinkNumber)
                rinkDay.add(event);
        return  rinkDay;
    }

    public void removeExpiredEvents() {
        for (ArrayList<Event> eventArrayList : events)
            eventArrayList.removeIf(event -> LocalDateTime.now().isAfter(event.getStartTime()));
    }

    public void addEvent(Event event, boolean assignLockers, int dayOfWeek) {
        //We need to subtract by 1 because getting the days of the week start at 1.
        dayOfWeek--;
        event.setId(getNextAvailableId(dayOfWeek));
        events[dayOfWeek].add(event);

        if(assignLockers) {
            //We only want the first event because this will be the first event added.
            events[dayOfWeek] = sorter.sortEventsAndAssignLockers(events[dayOfWeek], event, dayOfWeek);
        }else {
            events[dayOfWeek] = sorter.sortEvents(events[dayOfWeek]);
        }
    }

    public void updateEvent(Event event, boolean assignLockers, int dayOfWeek){
        //We need to subtract by 1 because getting the days of the week start at 1.
        dayOfWeek--;
        ArrayList<Event> dayEvents = events[dayOfWeek];
        for(int i = 0; i < dayEvents.size(); i++) {
            if (dayEvents.get(i).getId() == event.getId())
                dayEvents.set(i, event);
        }
        if(assignLockers)
            events[dayOfWeek] = sorter.sortEventsAndAssignLockers(dayEvents, event, dayOfWeek);
        else
            events[dayOfWeek] = sorter.sortEvents(dayEvents);
    }

    public boolean doesEventExist(Event event, int dayOfWeek) {
        //We need to subtract by 1 because getting the days of the week start at 1.
        ArrayList<Event> dayEvents = events[dayOfWeek - 1];
        for (Event e : dayEvents)
            if (e.getId() == event.getId())
                return true;
        return false;
    }

    public Event findEvent(Event event, int dayOfWeek) {
        //We need to subtract by 1 because getting the days of the week start at 1.
        ArrayList<Event> dayEvents = events[dayOfWeek - 1];
        for (Event e : dayEvents)
            if (e.getId() == event.getId())
                return e;
        return event;
    }

    public int getNextAvailableId(int dayOfWeek) {
        int availableId = 0;
        boolean notFound = false;
        //We need to subtract by 1 because getting the days of the week start at 1.
        ArrayList<Event> dayEvents = events[dayOfWeek];

        while(!notFound) {
            notFound = true;
            for (Event event : dayEvents)
                if (availableId == event.getId()) {
                    notFound = false;
                    availableId++;
                }
        }

        return availableId;
    }

    public void removeEvent(Event removedEvent, int dayOfWeek){
        //We need to subtract by 1 because getting the days of the week start at 1.
        //Although find event will subtract one itself
        events[dayOfWeek - 1].remove(findEvent(removedEvent, dayOfWeek));
    }

    public void resetCollection(){
        for(ArrayList<Event> list : events)
            list.clear();
    }

}
