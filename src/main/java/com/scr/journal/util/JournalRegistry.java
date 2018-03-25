package com.scr.journal.util;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class JournalRegistry {

    private final DataPersister<Journals> journalPersister;
    private final DataLoader<Journals> backupLoader;

    private List<Journal> journals;

    public JournalRegistry(
            DataLoader<Journals> journalLoader,
            DataPersister<Journals> journalPersister,
            DataLoader<Journals> backupLoader) {
        this.journalPersister = journalPersister;
        this.backupLoader = backupLoader;
        setJournals(journalLoader.load());
        sort();
    }

    public Collection<Year> getDistinctYears() {
        return getJournals().stream()
                .map(journal -> Year.from(journal.getDate()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private void setJournals(Journals journals) {
        this.journals = new ArrayList<>();
        if (journals != null) {
            this.journals.addAll(journals.getJournals());
        }
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

    public void resetToBackup() {
        // Todo: Implement backup handling on a year-basis
        setJournals(backupLoader.load());
        persist();
    }

    private void sort() {
        Collections.sort(journals);
    }

    private void persist() {
        journalPersister.persist(Journals.from(journals));
    }

}
