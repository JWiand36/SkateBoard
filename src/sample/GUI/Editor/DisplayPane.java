package sample.GUI.Editor;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sample.Controller;
import sample.Model.Event;

import java.util.ArrayList;
import java.util.Calendar;

public class DisplayPane extends VBox {

    private final TableView<EventModel> table = new TableView<>();
    private final Controller controller;

    public DisplayPane(Controller controller) {
        this.controller = controller;

        TableColumn<EventModel, String> team1Col = new TableColumn<>("Team 1");
        TableColumn<EventModel, String> lock1Col = new TableColumn<>("Locker 1");
        TableColumn<EventModel, String> team2Col = new TableColumn<>("Team 2");
        TableColumn<EventModel, String> lock2Col = new TableColumn<>("Locker 2");
        TableColumn<EventModel, String> rinkCol = new TableColumn<>("Rink");
        TableColumn<EventModel, String> timeCol = new TableColumn<>("Time");
        TableColumn<EventModel, String> endDayCol = new TableColumn<>("Last Day");

        team1Col.setPrefWidth(200);
        team2Col.setPrefWidth(200);

        team1Col.setCellValueFactory(new PropertyValueFactory<>("team1"));
        team2Col.setCellValueFactory(new PropertyValueFactory<>("team2"));
        lock1Col.setCellValueFactory(new PropertyValueFactory<>("locker1"));
        lock2Col.setCellValueFactory(new PropertyValueFactory<>("locker2"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endDayCol.setCellValueFactory(new PropertyValueFactory<>("lastDay"));
        rinkCol.setCellValueFactory(new PropertyValueFactory<>("rinkNum"));

        team1Col.setVisible(true);
        team2Col.setVisible(true);
        lock1Col.setVisible(true);
        lock2Col.setVisible(true);
        rinkCol.setVisible(true);
        timeCol.setVisible(true);
        endDayCol.setVisible(true);

        table.setRowFactory(
                new Callback<TableView<EventModel>, TableRow<EventModel>>() {
                    @Override
                    public TableRow<EventModel> call(TableView<EventModel> tableView) {
                        final TableRow<EventModel> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();

                        MenuItem editItem = new MenuItem("Edit Event");
                        editItem.setOnAction(e -> editEvent(row.getItem()));

                        MenuItem removeItem = new MenuItem("Delete Event");
                        removeItem.setOnAction(e -> {
                            removeEvent(row.getItem());
                            table.getItems().remove(row.getItem());
                        });

                        rowMenu.getItems().addAll(editItem, removeItem);

                        // only display context menu for non-empty rows:
                        row.contextMenuProperty().bind(
                                Bindings.when(row.emptyProperty())
                                        .then((ContextMenu) null)
                                        .otherwise(rowMenu));
                        return row;
                    }
                });


        table.setEditable(false);
        table.getSortOrder().addAll(timeCol);
        table.getColumns().addAll(team1Col, lock1Col, team2Col, lock2Col, rinkCol, timeCol, endDayCol);

        this.getChildren().addAll(table);

        setTable(Calendar.MONDAY);
    }

    public void setTable(int dayOfWeek){
        ArrayList<Event> events = controller.getDay(dayOfWeek);
        ArrayList<EventModel> displayEvents = new ArrayList<>();

        for(Event event : events)
            displayEvents.add(new EventModel(event));

        ObservableList<EventModel> currentDay = FXCollections.observableArrayList(displayEvents);
        table.setItems(currentDay);
    }

    private void removeEvent(EventModel event) { controller.removeEvent(event.returnEvent());}

    private void editEvent(EventModel event) { controller.editEvent(event.returnEvent()); }
}
