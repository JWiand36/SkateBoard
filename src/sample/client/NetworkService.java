package sample.client;

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
    private EventCollection eventCollection;

    NetworkService(Client client, EventCollection eventCollection){
        this.client = client;
        this.eventCollection = eventCollection;
    }

    //Retrieves the data from the server
    void inputData() throws IOException, ClassNotFoundException {

        Socket socket = new Socket(ip, 36);

        //The streams are in order with the server. Even if it isn't used
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(1);
        eventCollection.combineEvents((ArrayList<Event>[]) objInput.readObject());
        socket.close();
    }

    //Sends the data to the Server
    void outputData() throws IOException {
        Socket socket = new Socket(ip, 36);

        //The streams are in order with the server. Even if it isn't used
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());

        output.writeByte(2);
        objOutput.writeObject(eventCollection.getSeparatedEvents());

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

    void setIPAddress(String ip){
        this.ip = ip;
    }

    String getIPAddress(){
        return ip;
    }

}
