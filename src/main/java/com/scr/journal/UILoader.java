package com.scr.journal;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ResourceBundle;

public class UILoader {

    private final FXMLLoader prototypeFxmlLoader;
    private final String version;
    private Stage stage;

    public UILoader(FXMLLoader prototypeFxmlLoader, String version) {
        this.prototypeFxmlLoader = prototypeFxmlLoader;
        this.version = version;
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
            stage.getIcons().addAll(
                    new Image(UILoader.class.getResourceAsStream("/icon/deer16.png")),
                    new Image(UILoader.class.getResourceAsStream("/icon/deer32.png")),
                    new Image(UILoader.class.getResourceAsStream("/icon/deer48.png")),
                    new Image(UILoader.class.getResourceAsStream("/icon/deer64.png")));
            stage.setTitle("Journal Manager - " + version);
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
