package com.scr.journal.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertBuilder {

    private Alert alert;

    public static AlertBuilder alert(Alert.AlertType alertType) {
        AlertBuilder builder = new AlertBuilder();
        builder.alert = new Alert(alertType);
        return builder;
    }

    public AlertBuilder withTitle(String title) {
        alert.setTitle(title);
        return this;
    }

    public AlertBuilder withHeader(String headerText) {
        alert.setHeaderText(headerText);
        return this;
    }

    public AlertBuilder withContent(String contentText) {
        alert.setContentText(contentText);
        return this;
    }

    public void show() {
        alert.show();
    }

    public Optional<ButtonType> showAndWait() {
        return alert.showAndWait();
    }

}
