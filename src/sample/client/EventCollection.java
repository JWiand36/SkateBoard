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
}
