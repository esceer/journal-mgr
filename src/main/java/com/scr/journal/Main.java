package com.scr.journal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.scr.journal.annotations.JournalPersister;
import com.scr.journal.config.JournalModule;
import com.scr.journal.dao.DataBackupHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Injector injector = Guice.createInjector(new JournalModule());
        DataBackupHandler dataBackupHandler = injector.getInstance(Key.get(new TypeLiteral<DataBackupHandler>() {}, JournalPersister.class));
        dataBackupHandler.createBackup();

        UILoader uiLoader = injector.getInstance(UILoader.class);
        uiLoader.start(primaryStage);
    }

}
