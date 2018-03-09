package com.scr.journal.util;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;

import java.util.*;

public class JournalRegistry {

    private final List<Journal> journals;

    private final DataPersister<Journals> journalPersister;

    public JournalRegistry(DataLoader<Journals> journalLoader, DataPersister<Journals> journalPersister) {
        this.journals = new ArrayList<>(journalLoader.load().getJournals());
        this.journalPersister = journalPersister;
        sort();
    }

    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(journals);
    }

    public void add(Journal journal) {
        add(Arrays.asList(journal));
    }

    public void add(Collection<Journal> journalsToAdd) {
        for (Journal journal : journalsToAdd) {
            journals.add(journal);
        }
        sort();
        persist();
    }

    public void remove(Journal journal) {
        journals.remove(journal);
        persist();
    }

    public void replace(Journal oldJournal, Journal newJournal) {
        journals.remove(oldJournal);
        add(newJournal);
    }

    private void sort() {
        Collections.sort(journals);
    }

    private void persist() {
        journalPersister.persist(Journals.from(journals));
    }

}
