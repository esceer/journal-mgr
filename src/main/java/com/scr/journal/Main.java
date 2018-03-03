package com.scr.journal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.scr.journal.config.JournalModule;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Injector injector = Guice.createInjector(new JournalModule());

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(injector::getInstance);

        try (InputStream inputStream = Main.class.getResourceAsStream("/com/scr/journal/views/journal_book.fxml")) {
            Parent root = fxmlLoader.load(inputStream);
            primaryStage.setTitle("Journal Manager");
            primaryStage.setScene(new Scene(root, 960, 720));
            primaryStage.show();
        }
    }

    private void persistTestData(Injector injector) {
        Journals journals = new Journals(Arrays.asList(
                new Journal("2018.03.01", PaymentType.BANK_TRANSFER, PaymentDirection.INCOMING, "A12345678", 10000, "Random reason 1", "Random address 1", "1"),
                new Journal("2018.03.02", PaymentType.CASH, PaymentDirection.INCOMING, "A12345679", 10500, "Random reason 2", "Random address 2", "2")
        ));

        DataPersister<Journals> persister = injector.getInstance(new Key<DataPersister<Journals>>(){});
        persister.persist(journals);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
