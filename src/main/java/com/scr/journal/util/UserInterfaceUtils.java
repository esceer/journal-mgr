package com.scr.journal.util;

import com.scr.journal.model.Journal;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.BiConsumer;

public class UserInterfaceUtils {

    public static <T> void setCellEditable(
            TableView<Journal> tableView,
            TableColumn<Journal, T> column,
            BiConsumer<Journal, T> setter) {
        column.setOnEditCommit(event -> {
            final T value = event.getNewValue() != null
                    ? event.getNewValue()
                    : event.getOldValue();
            Journal journal = event.getTableView().getItems()
                    .get(event.getTablePosition().getRow());
            setter.accept(journal, value);
            tableView.refresh();
        });
    }

}
