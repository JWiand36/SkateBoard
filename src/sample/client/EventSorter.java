package sample.client;

import sample.Event;

import java.util.ArrayList;

class EventSorter {

    static ArrayList<Event> sortEvents(ArrayList<Event> events){
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

    //This sorts the am/pm periods
    private static ArrayList<sample.Event> sortPeriod(ArrayList<sample.Event> sortingPeriod ){

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
