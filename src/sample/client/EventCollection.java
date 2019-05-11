package sample.client;

import sample.Event;

import java.lang.reflect.Array;
import java.util.ArrayList;

class EventCollection {

    private ArrayList<Event>[] combinedEvents;
    private Client client;

    EventCollection(Client client){
        this.client = client;
        resetCollection();
    }

    void setEvents(ArrayList<Event>[] events){
        combinedEvents = events;
    }

    ArrayList<Event>[] getEvents(){
        return combinedEvents;
    }

    //The Event list coming from the server is 14 different ArrayLists but the client uses only 7, this method
    //separates the ArrayLists based on which rink they are used. Makes it easier to handle the data for the Server
    ArrayList<Event>[] getSeparatedEvents(){

        //Just checks if the first locker room is higher then 4. If the first locker is higher then 4 then the Event
        //takes place on Rink 2, otherwise the Event is on Rink 1.
        ArrayList<Event>[] result = new ArrayList[14];

        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < combinedEvents.length; i++){
            for(int k = 0; k < combinedEvents[i].size(); k++){

                if(combinedEvents[i].get(k).getLocker1()<5)
                    result[i].add(combinedEvents[i].get(k));
                else
                    result[i+7].add(combinedEvents[i].get(k));

            }
        }

        return result;
    }

    //The client manipulates the data with 7 ArrayLists but the Server uses 14 ArrayLists, makes it easier to manipulate
    //the data. This combines the data into 7 ArrayLists
    void combineEvents(ArrayList<Event>[] events){

        ArrayList<Event>[] result = new ArrayList[7];

        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < events.length; i++) {
            for (int k = 0; k < events[i].size(); k++)
                result[i % 7].add(events[i].get(k));
        }

        combinedEvents = result;
        client.updateLists(combinedEvents);
    }

    void addEvent(int day, Event event){
        combinedEvents[day].add(event);
        client.updateLists(combinedEvents);
    }

    void removeEvent(int day, int eventIndex){
        combinedEvents[day].remove(eventIndex);
    }

    Event getEvent(int day, int event){
        return combinedEvents[day].get(event);
    }

    void resetCollection(){
        combinedEvents = new ArrayList[7];

        for(int i = 0; i < combinedEvents.length; i++)
            combinedEvents[i] = new ArrayList<>();

        client.updateLists(combinedEvents);
    }

    void sortEvents(boolean assign){
        ArrayList<sample.Event> rink1 = new ArrayList<>();
        ArrayList<sample.Event> rink2 = new ArrayList<>();

        for(ArrayList<Event> events: combinedEvents){

            //Splits the data by rink
            for(Event event: events){
                if(event.getRinkNum() == 1)
                    rink1.add(event);
                else
                    rink2.add(event);
            }

            events.clear();
            if(assign)
                rink1 = assignLockers(sortEvents(rink1));
            events.addAll(rink1);
            if(assign)
                rink2 = assignLockers(sortEvents(rink2));
            events.addAll(sortEvents(rink2));
            rink1.clear();
            rink2.clear();
        }
    }

    private ArrayList<Event> assignLockers(ArrayList<Event> events){

        for(int i = 0; i < events.size(); i++) {
            if (events.get(i).getRinkNum() == 1) {
                if (i % 2 == 0) {
                    events.get(i).setLocker1(1);
                    if (events.get(i).getTeam2() != null)
                        events.get(i).setLocker2(3);
                } else {
                    events.get(i).setLocker1(2);
                    if (events.get(i).getTeam2() != null)
                        events.get(i).setLocker2(4);
                }
            }else{
                if (i % 2 == 0) {
                    events.get(i).setLocker1(5);
                    if (events.get(i).getTeam2() != null)
                        events.get(i).setLocker2(7);
                } else {
                    events.get(i).setLocker1(6);
                    if (events.get(i).getTeam2() != null)
                        events.get(i).setLocker2(8);
                }
            }
        }

        return events;
    }

    private ArrayList<Event> sortEvents(ArrayList<Event> events){
        ArrayList<sample.Event> am = new ArrayList<>();
        ArrayList<sample.Event> pm = new ArrayList<>();

        for(Event event: events){
            if(event.getDayNightCycle().equals("am"))
                am.add(event);
            else
                pm.add(event);
        }

        events.clear();
        events.addAll(sortPeriod(am));
        events.addAll(sortPeriod(pm));

        return events;
    }

    private ArrayList<sample.Event> sortPeriod(ArrayList<sample.Event> sortingPeriod ){

        ArrayList<Event> sortedList = new ArrayList<>();
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
