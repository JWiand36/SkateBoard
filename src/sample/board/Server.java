package sample.board;

import javafx.application.Platform;
import sample.Event;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{

    private int stop = 1;
    private Main main;

    Server(Main main){
        this.main = main;
    }

    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(36);

            while(stop == 1) {

                //Looks for the communication of the client, the Input/Output Stream must be in order
                //Input, Input, Output
                Socket socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());


                byte selection = input.readByte();


                ArrayList<Event>[] events;
                //Sends the data
                if(selection == 1){
                    events = main.getEvents();
                    objOutput.writeObject(events);
                    objOutput.flush();
                    objOutput.close();
                }

                //Receive the data
                if(selection == 2) {
                    events = (ArrayList<Event>[]) objInput.readObject();
                    main.setEvents(events);
                }

                //Sets the alert message if there is an emergency. The FlowPane is red to attract attention
                if(selection == 3){
                    main.setAlertMessage((String)objInput.readObject());
                    Platform.runLater(()-> main.displayAlertMessage(true));
                }

                //Hides the alert message if the emergency is over.
                if(selection == 4){
                    Platform.runLater(()->main.displayAlertMessage(false));
                }

                //Displays a promotional message that scrolls on the bottom
                if(selection == 5){
                    main.setPromotionalMessage((String)objInput.readObject());
                }

                socket.close();
            }
        }catch (IOException | ClassNotFoundException e){
            stop = 0;
            e.printStackTrace();
        }

    }

    void stop(){ stop = 0;}
}