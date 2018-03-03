package com.scr.journal.model;

import java.beans.ConstructorProperties;
import java.util.Collection;

public class Journals {

    private Collection<Journal> journals;

    @ConstructorProperties("journal")
    public Journals(Collection<Journal> journals) {
        this.journals = journals;
    }

    public static Journals from(Collection<Journal> journals) {
        return new Journals(journals);
    }

    public Collection<Journal> getJournals() {
        return journals;
    }

}
