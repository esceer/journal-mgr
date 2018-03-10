package com.scr.journal.config;

public class Settings {

    private final String language;
    private final String country;

    public Settings(
            String language,
            String country) {
        this.language = language;
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

}
