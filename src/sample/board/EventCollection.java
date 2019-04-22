package sample.board;

import sample.Event;

import java.io.IOException;
import java.util.ArrayList;

public class EventCollection {


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

    //Sorts the arrays of Events to be in ascending order of time, it uses the merge sorting technique.
    void sortEvents(){
        System.out.println("Sorting");
        ArrayList<sample.Event>[] result = new ArrayList[events.length];
        ArrayList<sample.Event>[] sortArrays = new ArrayList[12];
        ArrayList<sample.Event> am = new ArrayList<>();
        ArrayList<sample.Event> pm = new ArrayList<>();

        //sets up the result and the array used to sort the data
        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < sortArrays.length; i++)
            sortArrays[i] = new ArrayList<>();

        for(int i = 0; i < result.length; i++){

            //Splits the data from Day/Night Cycle, AM will be completed first
            for(int k = 0; k < events[i].size(); k++){
                if(events[i].get(k).getDayNightCycle().equals("am"))
                    am.add(events[i].get(k));
                else
                    pm.add(events[i].get(k));

            }

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < am.size(); k++)
                sortArrays[am.get(k).getStartHour()%12].add(am.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int index = 0;
                while(sortArrays[k].size() > 1){
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(index).getStartMin() > sortArrays[k].get(c).getStartMin()){
                            index = sortArrays[k].indexOf(sortArrays[k].get(c));
                        }
                    }
                    result[i].add(sortArrays[k].get(index));
                    sortArrays[k].remove(index);
                    index = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            am.clear();

            //Puts the data in the arrays based on the hour. hour 1 goes in array[1]
            for(int k = 0; k < pm.size(); k++)
                sortArrays[pm.get(k).getStartHour()%12].add(pm.get(k));


            //Goes through the list of Arrays and puts it in the result. If there are multiple hours, sorts by the Min.
            for(int k = 0; k < sortArrays.length; k++){
                int index = 0;
                while(sortArrays[k].size() > 1){
                    System.out.println("Size: " + sortArrays[k].size()+", Index: "+k);
                    for(int c = 1; c < sortArrays[k].size(); c++) {
                        if(sortArrays[k].get(index).getStartMin() > sortArrays[k].get(c).getStartMin()){
                            index = sortArrays[k].indexOf(sortArrays[k].get(c));
                        }
                    }
                    result[i].add(sortArrays[k].get(index));
                    sortArrays[k].remove(index);
                    index = 0;
                }

                if(sortArrays[k].size() == 0)
                    continue;

                result[i].add(sortArrays[k].get(0));
                sortArrays[k].clear();
            }
            pm.clear();
        }
        this.events = result;

        main.displayNewDay();
    }

    ArrayList<Event>[] getEvents(){ return events; }

    ArrayList<Event> getWeek(int weekDayNumber){ return events[weekDayNumber]; }
}
