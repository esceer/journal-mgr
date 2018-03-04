package com.scr.journal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.scr.journal.config.JournalModule;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journals;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Injector injector = Guice.createInjector(new JournalModule());
        DataPersister<Journals> persister = injector.getInstance(new Key<DataPersister<Journals>>() {});
        persister.createBackup();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(injector::getInstance);

        try (InputStream inputStream = Main.class.getResourceAsStream("/com/scr/journal/views/journal_book.fxml")) {
            Parent root = fxmlLoader.load(inputStream);
            primaryStage.setTitle("Journal Manager");
            primaryStage.setScene(new Scene(root, 960, 720));
            primaryStage.show();
        }
    }

}
