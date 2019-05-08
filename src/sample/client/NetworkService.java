package sample.client;

import javafx.collections.FXCollections;
import sample.Event;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

class NetworkService {

    private Client client;
    private String ip = "localhost";

    NetworkService(Client client){
        this.client = client;
    }

    //Retrieves the data from the server
    ArrayList<Event>[] inputData() throws IOException, ClassNotFoundException {

        Socket socket = new Socket(ip, 36);

        //The streams are in order with the server. Even if it isn't used
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(1);
        ArrayList<Event>[] combinedEvents = combineEvents((ArrayList<Event>[]) objInput.readObject());
        updateLists(combinedEvents);
        socket.close();
        return combinedEvents;
    }

    //Sends the data to the Server
    void outputData() throws IOException {
        Socket socket = new Socket(ip, 36);

        //The streams are in order with the server. Even if it isn't used
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(2);
        objOutput.writeObject(separateEvents(client.getCombinedEvents()));

        objOutput.flush();
        objOutput.close();
        socket.close();
    }

    //Sends the Alert message to the server
    void sendAlertMessage(String message) throws IOException {
        try {
            Socket socket = new Socket(ip, 36);

            //The streams are in order with the server. Even if it isn't used
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

            output.writeByte(3);
            objOutput.writeObject(message);

            objOutput.flush();
            objOutput.close();
            socket.close();
        }catch (IOException io){io.printStackTrace();}
    }

    //Removes the alert message on the Server
    void removeAlertMessage() throws IOException {
        try {
            Socket socket = new Socket(ip, 36);

            //The streams are in order with the server. Even if it isn't used
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

            output.writeByte(4);
            socket.close();


        } catch (IOException io) {io.printStackTrace();}

    }

    //Sends a Promotional message to the server
    void sendMessage(String message)throws IOException {
        try {
            Socket socket = new Socket(ip, 36);

            //The streams are in order with the server. Even if it isn't used
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

            output.writeByte(5);
            objOutput.writeObject(message);

            objOutput.flush();
            objOutput.close();
            socket.close();
        }catch (IOException io){io.printStackTrace();}
    }

    //The Event list coming from the server is 14 different ArrayLists but the client uses only 7, this method
    //separates the ArrayLists based on which rink they are used. Makes it easier to handle the data for the Server
    private ArrayList<Event>[] separateEvents(ArrayList<Event>[] combinedEvents){

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
    private ArrayList<Event>[] combineEvents(ArrayList<Event>[] events){

        ArrayList<Event>[] result = new ArrayList[7];

        for(int i = 0; i < result.length; i++)
            result[i] = new ArrayList<>();

        for(int i = 0; i < events.length; i++) {
            for (int k = 0; k < events[i].size(); k++)
                result[i % 7].add(events[i].get(k));
        }

        return result;
    }

    //Updates the lists on the MainPane
    void updateLists(ArrayList<Event>[] combinedEvents){

        ArrayList<String>[] names = new ArrayList[7];

        for (int i = 0; i < combinedEvents.length; i++) {
            names[i] = new ArrayList<>();

            for (int k = 0; k < combinedEvents[i].size(); k++)
                if(combinedEvents[i].get(k).getTeam2() != null)
                    names[i].add(combinedEvents[i].get(k).getTeam1() + " vs " + combinedEvents[i].get(k).getTeam2());
                else
                    names[i].add(combinedEvents[i].get(k).getTeam1());

            client.getLists()[i].setItems(FXCollections.observableArrayList(names[i]));
        }
    }

    void setIPAddress(String ip){
        this.ip = ip;
    }

    String getIPAddress(){
        return ip;
    }

}
