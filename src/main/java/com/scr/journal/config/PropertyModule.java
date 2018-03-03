package com.scr.journal.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.scr.journal.util.PropertyLoader;

import java.util.Properties;

public class PropertyModule extends AbstractModule {

    @Override
    protected void configure() {
        loadProperties();
    }

    private void loadProperties() {
        Properties properties = PropertyLoader.loadProperties("/com/scr/journal/config/app.properties");
        Names.bindProperties(binder(), properties);
    }

}
