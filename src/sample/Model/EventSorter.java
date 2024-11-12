package sample.Model;

import java.util.ArrayList;
import java.util.Calendar;

class EventSorter {

    private final LockerAssigner lockerAssigner = new LockerAssigner();

    ArrayList<Event> sortDailyEvents(ArrayList<Event> events) {
        ArrayList<Event> am = new ArrayList<>();
        ArrayList<Event> pm = new ArrayList<>();

        for (Event event : events) {
            if (event.isAm())
                am.add(event);
            else
                pm.add(event);
        }

        events.clear();
        //Sort based on time and combine each event to 1 list
        events.addAll(sortPeriod(am));
        events.addAll(sortPeriod(pm));

        return events;
    }

    ArrayList<Event> sortEventsAndAssignLockers(ArrayList<Event> events, Event newEvent, int dayOfWeek) {
        ArrayList<Event> weekEvents = new ArrayList<>(events);

        //We have to subtract by one because DAY_OF_WEEK starts with 1
        //weekEvents[newEvent.getStartTime().get(Calendar.DAY_OF_WEEK) - 1] = lockerAssigner.assignLockers(sortDailyEvents(sortingEvents), newEvent);
        weekEvents = lockerAssigner.assignLockers(sortEvents(weekEvents), newEvent);

        return weekEvents;
    }

    //This sorts the am/pm periods
    private ArrayList<Event> sortPeriod(ArrayList<Event> sortingPeriod ){

        ArrayList<Event> sortedList = new ArrayList<>();
        ArrayList<Event>[] hours = new ArrayList[12];

        for(int i = 0; i < hours.length; i++)
            hours[i] = new ArrayList<>();


        //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
        for(Event event: sortingPeriod)
            hours[event.getStartTime().getHour() % 12].add(event);


        //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
        for(ArrayList<Event> hourList: hours){
            int index = 0;
            while(hourList.size() > 1){
                for(int c = 1; c < hourList.size(); c++) {
                    if(hourList.get(index).getStartTime().getMinute() > hourList.get(c).getStartTime().getMinute()){
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

    ArrayList<Event> sortEvents(ArrayList<Event> events) {

        ArrayList<Event> sortedEvents = new ArrayList<>();

        while(!events.isEmpty()) {

            int index = 0;

            if(events.size() > 1){

                for(Event selectedEvent : events)
                    if(selectedEvent.getStartTime().toLocalTime().isBefore(events.get(index).getStartTime().toLocalTime()))
                        index = events.indexOf(selectedEvent);
            }

            sortedEvents.add(events.get(index));
            events.remove(index);
        }

        return sortedEvents;
    }
}
