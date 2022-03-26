package sample.Model;

import java.util.ArrayList;

public class LockerAssigner {


    public ArrayList<Event> assignLockers(ArrayList<Event> events, Event firstNewEvent){

        boolean assign = false;

        if(firstNewEvent != null){
            if(firstNewEvent.getLocker2() < 1){
                firstNewEvent.setLocker2(firstNewEvent.getLocker1());
            }

            for(int i = 0; i < events.size(); i++){
                if(assign && events.get(i).getRinkNum() == firstNewEvent.getRinkNum()){

                    Event upperEvent = firstNewEvent;
                    for(int j = i - 1; j >= 0; j--){
                        if(events.get(j).getRinkNum() == firstNewEvent.getRinkNum()) {
                            upperEvent = events.get(j);
                            break;
                        }
                    }

                    setLockerFromAbove(events.get(i), upperEvent, events.get(i).equals(firstNewEvent));
                }else if(events.get(i).equals(firstNewEvent)) {
                    assign = true;
                    if (i == 0) {
                        if (firstNewEvent.getLocker1() <= 0)
                            setLockerGroup(events.get(i), 1);
                    } else if (firstNewEvent.getLocker1() <= 0) {
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
        if (event.getTeam2() != null && !event.getTeam2().contentEquals("") && !event.getTeam2().contentEquals(" "))
            event.setLocker2((lockerNum + 2) + 4 * (event.getRinkNum() - 1));
        else
            event.setLocker2(lockerNum + 4 * (event.getRinkNum() - 1));
    }

    private void setLockerFromAbove(Event editingEvent, Event upperEvent, boolean firstEvent){

        boolean upperSideBySide = (upperEvent.getLocker1()+1)%(upperEvent.getLocker2()+1) == 1 || (upperEvent.getLocker2()+1)%(upperEvent.getLocker1()+1) == 1;

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
