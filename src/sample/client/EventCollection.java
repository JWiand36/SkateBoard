package sample.client;

import sample.Event;

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

    void sortEvents(boolean assign, Event firstRink1, Event firstRink2){
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
                rink1 = assignLockers(EventSorter.sortEvents(rink1), firstRink1);
            else
                rink1 = EventSorter.sortEvents(rink1);
            events.addAll(rink1);

            if(assign)
                rink2 = assignLockers(EventSorter.sortEvents(rink2), firstRink2);
            else
                rink2 = EventSorter.sortEvents(rink2);
            events.addAll(rink2);
            rink1.clear();
            rink2.clear();
        }
    }

    private ArrayList<Event> assignLockers(ArrayList<Event> events, Event firstNewEvent){

        boolean assign = false;

        if(firstNewEvent != null){
            for(int i = 0; i < events.size(); i++){
                if(assign){
                    setLockerFromAbove(events.get(i), events.get(i-1), events.get(i).equals(firstNewEvent));
                }else if(events.get(i).equals(firstNewEvent)) {
                    assign = true;
                    if (i == 0) {
                        if (firstNewEvent.getLocker1() == -1)
                            setLockerGroup(events.get(i), 1);
                    } else if (firstNewEvent.getLocker1() == -1) {
                        setLockerFromAbove(events.get(i), events.get(i - 1), false);
                    } else {
                        setLockerFromAbove(events.get(i), events.get(i - 1), events.get(i).equals(firstNewEvent));
                    }
                }
            }
       }
//      else
//            for(int i = 0; i < events.size(); i++) {
//                if (i % 2 == 0) {
//                    //(Rink number - 1) * 4 should produce 0 for Rink1 and 1 for Rink2. Then add the locker slot 1-4
//                    //With rink 1 is should be 1-4 and with rink 2 it will be 5-8
//                    setLockerGroup(events.get(i), 1);
//                } else {
//                    setLockerGroup(events.get(i), 2);
//                }
//            }

        return events;
    }

    /*
    This is used to set the locker groups 1/3, 2/4, 5/7 and 6/8. The locker number should be the first locker and you
    don't need use 5 or 6 for the later groups, only 1 or 2 is needed. For 5/7 and 6/8 the rink number of the event
    will produce the 5/6 groups.
    */
    private void setLockerGroup(Event event, int lockerNum){
        event.setLocker1(lockerNum + 4 * (event.getRinkNum() - 1));
        if (event.getTeam2() != null)
            event.setLocker2((lockerNum + 2) + 4 * (event.getRinkNum() - 1));
    }

    private void setLockerFromAbove(Event editingEvent, Event upperEvent, boolean firstEvent){

        boolean upperSideBySide = upperEvent.getLocker1() == 1 && upperEvent.getLocker2() == 2 || upperEvent.getLocker2()%upperEvent.getLocker1() == 1;

        if(!firstEvent) {
            if (upperSideBySide && upperEvent.getTeam2() != null) {
                System.out.println("Side by side");
                if (upperSideBySide) {
                    editingEvent.setLocker1(getSideBySideLocker(upperEvent.getLocker1()));
                    if (editingEvent.getTeam2() != null) {
                        if (upperEvent.getLocker2() != 0)
                            editingEvent.setLocker2(getSideBySideLocker(upperEvent.getLocker2()));
                        else
                            editingEvent.setLocker2(getSideBySideLocker(editingEvent.getLocker1()));
                    }
                }
            } else {
                System.out.println("Run");
                if (upperEvent.getLocker1() % 4 == 3 || upperEvent.getLocker1() % 4 == 1) {
                    setLockerGroup(editingEvent, 2);
                } else {
                    setLockerGroup(editingEvent, 1);
                }
            }
        }
    }

    private int getSideBySideLocker(int upperLocker){
        if(upperLocker == 1){
            return 3;
        }else if(upperLocker == 2){
            return 4;
        }else if(upperLocker == 3){
            return 1;
        }else if(upperLocker == 4){
            return 2;
        }else if(upperLocker == 5){
            return 7;
        }else if(upperLocker == 6){
            return 8;
        }else if(upperLocker == 7){
            return 5;
        }else{
            return 6;
        }
    }
}
