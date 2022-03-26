package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import sample.GUI.Editor.MenuPane;
import sample.GUI.Editor.DisplayPane;
import sample.GUI.Editor.ModifyPane;
import sample.Model.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

//The event editor is the main border pane that puts everything together.
public class EventEditor extends BorderPane{

    private final DisplayPane displayPane;
    private final ModifyPane modifyPane;
    private final ComboBox<String> daysOfWeek;
    private final Map<String, DayOfWeek> days = new HashMap<>();

    //Sets up the view of the MainPane
    public EventEditor(Controller controller) {

        this.setPadding(new Insets(5));
        displayPane = new DisplayPane(controller);
        modifyPane = new ModifyPane(controller);

        days.put("Sunday", DayOfWeek.SUNDAY);
        days.put("Monday", DayOfWeek.MONDAY);
        days.put("Tuesday", DayOfWeek.TUESDAY);
        days.put("Wednesday", DayOfWeek.WEDNESDAY);
        days.put("Thursday", DayOfWeek.THURSDAY);
        days.put("Friday", DayOfWeek.FRIDAY);
        days.put("Saturday", DayOfWeek.SATURDAY);

        ObservableList<String> list = FXCollections.observableArrayList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        daysOfWeek = new ComboBox<>(list);

        daysOfWeek.getSelectionModel().select(LocalDate.now().getDayOfWeek().getValue() - 1);
        daysOfWeek.setOnAction(e->{
            displayPane.setTable(days.get(daysOfWeek.getSelectionModel().getSelectedItem()).getValue());
            modifyPane.setDatePicker(days.get(daysOfWeek.getSelectionModel().getSelectedItem()));
        });

        VBox pane = new VBox(daysOfWeek, displayPane);

        this.setTop(new MenuPane(controller, this));
        this.setCenter(pane);
        this.setRight(modifyPane);
    }

    public DayOfWeek getDayOfWeek(){return days.get(daysOfWeek.getSelectionModel().getSelectedItem());}

    public boolean getAssignLockers(){ return modifyPane.getAssignLockers(); }

    public void changeDay() {
        displayPane.setTable(getDayOfWeek().getValue());
    }

    public void saveEvent(){ displayPane.setTable(getDayOfWeek().getValue()); }

    public void editEvent(Event modifyingEvent){ modifyPane.editEvent(modifyingEvent); }
}
