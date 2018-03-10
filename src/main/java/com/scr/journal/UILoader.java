package com.scr.journal;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ResourceBundle;

public class UILoader {

    private final FXMLLoader prototypeFxmlLoader;
    private Stage stage;

    public UILoader(FXMLLoader prototypeFxmlLoader) {
        this.prototypeFxmlLoader = prototypeFxmlLoader;
    }

    private FXMLLoader cloneLoader() {
        FXMLLoader clone = new FXMLLoader();
        clone.setControllerFactory(prototypeFxmlLoader.getControllerFactory());
        clone.setResources(prototypeFxmlLoader.getResources());
        return clone;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        reload();
    }

    public void reload() {
        if (stage == null) {
            throw new IllegalStateException("UI has not yet been started. Please run start before triggering a reaload");
        }
        try (InputStream inputStream = UILoader.class.getResourceAsStream("/com/scr/journal/views/journal_book.fxml")) {
            FXMLLoader fxmlLoader = cloneLoader();
            Parent root = fxmlLoader.load(inputStream);
            stage.setTitle("Journal Manager");
            stage.setScene(new Scene(root, 960, 720));
            stage.show();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ResourceBundle getResourceBundle() {
        return this.prototypeFxmlLoader.getResources();
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.prototypeFxmlLoader.setResources(resourceBundle);
    }
}
