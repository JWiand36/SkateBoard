package sample.board;

import javafx.application.Platform;
import javafx.scene.text.Font;
import sample.Event;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{

    private int stop = 1;
    private ArrayList<Event>[] events;
    private Main main;
    private Clock clock;

    Server(Main main, Clock clock){
        this.main = main;
        this.clock = clock;
        this.events = main.getEvents();
    }

    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(36);

            while(stop == 1) {

                //Looks for the communication of the client, the Input/Output Stream must be in order
                Socket socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objOutput = new ObjectOutputStream(socket.getOutputStream());


                byte selection = input.readByte();

                //Sends the data
                if(selection == 1){
                    objOutput.writeObject(events);
                    objOutput.flush();
                    objOutput.close();
                }

                //Receive the data
                if(selection == 2) {
                    events = (ArrayList<Event>[]) objInput.readObject();
                    main.sortEvents(events);
                }

                //Sets the alert message if there is an emergency. The FlowPane is red to attract attention
                if(selection == 3){
                    main.setSpecialMessage((String)objInput.readObject());
                    Platform.runLater(()-> main.displaySpecialMessage(true));
                }

                //Hides the alert message if the emergency is over.
                if(selection == 4){
                    Platform.runLater(()->main.displaySpecialMessage(false));
                }

                //Displays a promotional message that scrolls on the bottom
                if(selection == 5){
                    main.runMessage((String)objInput.readObject());
                }

                socket.close();
            }
        }catch (IOException | ClassNotFoundException e){e.printStackTrace(); new Thread(new Server(main, clock)).start();}

    }

    void stop(){
        stop = 0;
    }
}