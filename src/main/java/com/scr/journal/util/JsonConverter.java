package com.scr.journal.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public final class JsonConverter {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        ParameterNamesModule module = new ParameterNamesModule();
//        module.addSerializer(...)
        OBJECT_MAPPER.registerModule(module);

        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static <T> void writeValue(OutputStream outputStream, T object) {
        processAndWrapCheckedException(() -> {
            OBJECT_MAPPER.writeValue(outputStream, object);
            return null;
        });
    }

    public static <T> T readValue(InputStream inputStream, Class<T> valueType) {
        return processAndWrapCheckedException(() -> OBJECT_MAPPER.readValue(inputStream, valueType));
    }

    private static <T> T processAndWrapCheckedException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonConverter() {
        throw new UnsupportedOperationException();
    }

}
