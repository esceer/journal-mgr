package com.scr.journal.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

import java.io.IOException;

public class CustomMinimalPrettyPrinter extends MinimalPrettyPrinter {

    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
        super.writeArrayValueSeparator(g);
        g.writeRaw('\n');
    }
}
