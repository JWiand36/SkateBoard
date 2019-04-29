package sample.board;

import sample.Event;

import java.io.IOException;
import java.util.ArrayList;

class EventCollection {


    private ArrayList<sample.Event>[] events = new ArrayList[14];
    private Main main;

    EventCollection(Main main){
        this.main = main;

        for (int i = 0; i < events.length; i++)
            events[i] = new ArrayList<>();

        //Imports the events from file
        try{
            events = (ArrayList<sample.Event>[]) FileIO.readFromFile("Events");
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    void setEvents(ArrayList<Event>[] events){
        this.events = events;
        sortEvents();
    }

    ArrayList<Event>[] getEvents(){ return events; }

    ArrayList<Event> getDay(int weekDayNumber){ return events[weekDayNumber]; }

    void clearLastDay(int weekDayNumber){
        if (weekDayNumber != 0) {
            events[weekDayNumber - 1].clear();
            events[weekDayNumber + 6].clear();
        } else {
            events[weekDayNumber + 6].clear();
            events[weekDayNumber + 13].clear();
        }
    }
    //Sorts the arrays of Events to be in ascending order of time, it uses the merge sorting technique.

    private void sortEvents(){
        ArrayList<sample.Event>[] sortedList = new ArrayList[events.length];
        ArrayList<sample.Event> am = new ArrayList<>();
        ArrayList<sample.Event> pm = new ArrayList<>();

        for(int i = 0; i < sortedList.length; i++){

            sortedList[i] = new ArrayList<>();

            //Splits the data from Day/Night Cycle, AM will be completed first
            for(int k = 0; k < events[i].size(); k++){
                if(events[i].get(k).getDayNightCycle().equals("am"))
                    am.add(events[i].get(k));
                else
                    pm.add(events[i].get(k));

            }

            sortedList[i].addAll(sortPeriod(am));
            sortedList[i].addAll(sortPeriod(pm));
        }
        this.events = sortedList;

        main.displayNewDay();
    }

    private ArrayList<sample.Event> sortPeriod(ArrayList<sample.Event> sortingPeriod ){

        ArrayList<Event> sortedList = new ArrayList();
        ArrayList<Event>[] hours = new ArrayList[12];

        for(int i = 0; i < hours.length; i++)
            hours[i] = new ArrayList<>();


        //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
        for(Event event: sortingPeriod)
            hours[event.getStartHour() % 12].add(event);


        //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
        for(ArrayList<Event> hourList: hours){
            int index = 0;
            while(hourList.size() > 1){
                for(int c = 1; c < hourList.size(); c++) {
                    if(hourList.get(index).getStartMin() > hourList.get(c).getStartMin()){
                        index = hourList.indexOf(hourList.get(c));
                    }
                }
                sortedList.add(hourList.get(index));
                hourList.remove(index);
                index = 0;
            }

            if(hourList.size() == 0)
                continue;

            sortedList.add(hourList.get(0));
            hourList.clear();
        }
        sortingPeriod.clear();
        return sortedList;
    }
}
