package com.scr.journal.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsModule extends AbstractModule {

    @Provides
    @Singleton
    public Locale getLocale(
            @Named("system.locale.language") String localeLanguage,
            @Named("system.locale.country") String localeCountry) {
        return new Locale(localeLanguage, localeCountry);
    }

    @Provides
    @Singleton
    public ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle("com/scr/journal/config/ui_lang", locale);
    }

    @Provides
    @Singleton
    public NumberFormat getNumberFormat(
            @Named("system.number_format.language") String numberFormatLanguage,
            @Named("system.number_format.country") String numberFormatCountry) {
        return NumberFormat.getCurrencyInstance(new Locale(numberFormatLanguage, numberFormatCountry));
    }

}
