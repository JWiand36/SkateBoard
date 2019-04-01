package sample.board;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class PictureController {

    private String[] files;
    private ImageView[] pictures;
    private Pane imagePane = new Pane();
    private Thread thread;

    PictureController(){
        File dir = new File("Pictures");
        files = dir.list();

        imagePane.setPrefHeight(150);
    }

    //Collects a Random Image from the Picture folder in the project
    private void getImages(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < pictures.length; i++) {
                        int index = (int) (Math.random() * files.length);
                        if (files.length != 0) {
                            File f = new File("Pictures\\"+files[index]);
                            FileInputStream fi = new FileInputStream(f);
                            if (pictures[i].getImage() != null)
                                pictures[i].getImage().cancel();
                            pictures[i].setImage(new Image(fi));
                        }
                    }
                }catch(OutOfMemoryError | IOException mem){mem.printStackTrace();}
                thread = null;
            }
        });
        thread.start();
    }

    //Image work at the bottom of the program
    void runImage(){

        int amountOfPics = 3;
        //This gets the center of the screen
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double centerX = bounds.getWidth() / 2;
        try {

//        Timeline timeline = new Timeline();
            pictures = new ImageView[amountOfPics];
            for(int i = 0; i < pictures.length; i++)
                pictures[i] = new ImageView();

            getImages();

            //Sets up the display settings of the pictures
            for(int i = 0; i < amountOfPics; i++) {
                pictures[i].setFitWidth(375);
                pictures[i].setFitHeight(200);

                pictures[i].setY(-100);
                pictures[i].setX((centerX - 610) + i * 425);
                imagePane.getChildren().add(pictures[i]);
            }


            FadeTransition[] transitions = new FadeTransition[amountOfPics];
            ParallelTransition parallelTransition = new ParallelTransition();

            for (int i = 0; i < transitions.length; i++) {
                transitions[i] = new FadeTransition(Duration.seconds(10), pictures[i]);
                transitions[i].setToValue(1.0);
                transitions[i].setFromValue(0);
                parallelTransition.getChildren().add(transitions[i]);
            }

            parallelTransition.setOnFinished(e -> {
                if(transitions[amountOfPics-1].getToValue() == 1.0){
                    for (FadeTransition transition: transitions) {
                        transition.setToValue(0.0);
                        transition.setFromValue(1.0);
                    }
                }else{
                    for (int i = 0; i < transitions.length; i++) {
                        transitions[i].setToValue(1.0);
                        transitions[i].setFromValue(0);
                        getImages();
                    }
                }

                parallelTransition.play();
            });

            parallelTransition.play();
        }catch (Exception e){e.printStackTrace();}

    }

    Pane getImagePane(){
        return imagePane;
    }

}
