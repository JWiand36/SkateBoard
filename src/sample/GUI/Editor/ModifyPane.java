package sample.GUI.Editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sample.Controller;
import sample.Model.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class ModifyPane extends VBox {

    private final Controller controller;
    private Event modifyingEvent;

    private final TextField locker1TxtFld = new TextField();
    private final TextField locker2TxtFld = new TextField();
    private final TextField team1TxtFld = new TextField();
    private final TextField team2TxtFld = new TextField();
    private final TextField startTimeTxtFld = new TextField();
    private final TextField rinkNumTxtFld = new TextField();


    private final CheckBox assign;
    private final CheckBox amCheckBox = new CheckBox();

    private final DatePicker lastDayDatePicker = new DatePicker();

    public ModifyPane(Controller controller){

        this.controller = controller;

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        save.setOnAction(e->saveEvent());
        cancel.setOnAction(e->cancelEvent());

        locker1TxtFld.setText("0");
        locker2TxtFld.setText("0");
        rinkNumTxtFld.setText("0");

        this.getChildren().add(setUpGroupPane("Team 1: ", team1TxtFld, false));
        this.getChildren().add(setUpGroupPane("Team 2: ", team2TxtFld, false));
        this.getChildren().add(setUpGroupPane("Locker 1: ", locker1TxtFld, true));
        this.getChildren().add(setUpGroupPane("Locker 2: ", locker2TxtFld, true));
        this.getChildren().add(setUpGroupPane("Rink: ", rinkNumTxtFld, true));
        this.getChildren().add(setUpGroupPane("Start Time: ", startTimeTxtFld, false));

        HBox amPane = new HBox(new Label("AM: "), amCheckBox);
        amPane.setSpacing(5);
        amPane.setPadding(new Insets(5));
        amPane.setAlignment(Pos.CENTER_RIGHT);
        this.getChildren().add(amPane);

        lastDayDatePicker.setValue(LocalDate.now());
        lastDayDatePicker.getEditor().disableProperty();

        HBox datePane = new HBox(new Label("Last Day: "), lastDayDatePicker);
        datePane.setSpacing(5);
        datePane.setPadding(new Insets(5));
        datePane.setAlignment(Pos.CENTER_RIGHT);

        assign = new CheckBox("Assign Lockers");

        HBox buttonPane = new HBox(assign, save, cancel);
        buttonPane.setSpacing(5);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setAlignment(Pos.CENTER);

        this.getChildren().addAll(datePane, buttonPane);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(5));

        cancelEvent();
    }

    private void cancelEvent(){
        team1TxtFld.setText("");
        team2TxtFld.setText("");
        locker1TxtFld.setText("0");
        locker2TxtFld.setText("0");
        startTimeTxtFld.setText("");
        rinkNumTxtFld.setText("0");
        modifyingEvent = new Event("", 0, LocalDateTime.now());
        modifyingEvent.setId(-1);
        amCheckBox.setSelected(false);
        assign.setSelected(false);

        setDatePicker(controller.getWeekDay());
    }

    private void saveEvent() {


            modifyingEvent.setTeam1(team1TxtFld.getText());
            modifyingEvent.setTeam2(team2TxtFld.getText());

            modifyingEvent.setLocker1(Integer.parseInt(locker1TxtFld.getText()));
            modifyingEvent.setLocker2(Integer.parseInt(locker2TxtFld.getText()));
            modifyingEvent.setRink(Integer.parseInt(rinkNumTxtFld.getText()));

            LocalDate selectedDate = lastDayDatePicker.getValue();

        if(validateValues(modifyingEvent)) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("[hh:mma][h:mma][hh,mma][h,mma][hh.mma][h.mma][hhmma][hmma]", Locale.US);

                LocalTime selectedTime;
                if (amCheckBox.isSelected())
                    selectedTime = LocalTime.parse(startTimeTxtFld.getText() + "AM", dtf);
                else
                    selectedTime = LocalTime.parse(startTimeTxtFld.getText() + "PM", dtf);


                modifyingEvent.setStartTime(LocalDateTime.of(selectedDate, selectedTime));
                controller.saveEvent(modifyingEvent);
                cancelEvent();
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Time");
                alert.setHeaderText("Invalid Time");
                alert.setContentText("Time wasn't valid, options are: HH:mm - H:mm - HH,mm - H,mm - HH.mm - H.mm - HHmm - Hmm");
                alert.showAndWait();
            }
        }

    }

    private HBox setUpGroupPane (String lblText, TextField textField, boolean numbersOnly){
        HBox pane = new HBox(new Label(lblText), textField);

        pane.setAlignment(Pos.CENTER_RIGHT);
        pane.setPadding(new Insets(5));
        pane.setSpacing(5);

        if(numbersOnly){
            // force the field to be numeric only
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        textField.setText(newValue.replaceAll("[^\\d]", "0"));
                    }
                }
            });
        }

        return pane;
    }

    private boolean validateValues(Event event){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Rink Number");
        alert.setHeaderText("Invalid Value");

        if(event.getRinkNum() < 1 || event.getRinkNum() > 2){
            alert.setContentText("Rink number wasn't valid");
            alert.showAndWait();
            return false;
        }

        if(event.getLocker1() < 1 || event.getLocker1() > 8) {
            alert.setContentText("Locker 1 isn't valid");
            alert.showAndWait();
            return false;
        }

        return true;
    }

    public void editEvent(Event event){
        modifyingEvent = event;
        team1TxtFld.setText(event.getTeam1());
        team2TxtFld.setText(event.getTeam2());
        locker1TxtFld.setText(event.getLocker1()+"");
        locker2TxtFld.setText(event.getLocker2()+"");
        rinkNumTxtFld.setText(event.getRinkNum()+"");

        int hour = event.getStartTime().getHour();
        int min = event.getStartTime().getMinute();

        if(hour > 12)
            hour -= 12;
        else if(hour == 0)
            hour = 12;

        if(min == 0)
            startTimeTxtFld.setText( hour +":"+ min + "0");
        else
            startTimeTxtFld.setText( hour +":"+ min);

        amCheckBox.setSelected(event.isAm());

        LocalDate date = event.getStartTime().toLocalDate();
        date = date.with(TemporalAdjusters.nextOrSame(controller.getWeekDay()));
        lastDayDatePicker.setValue(date);
    }

    public boolean getAssignLockers(){ return assign.isSelected(); }

    public void setDatePicker(DayOfWeek dayOfWeek){
        LocalDate date = LocalDate.now();
        date = date.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        lastDayDatePicker.setValue(date);
    }
}
