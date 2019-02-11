package com.scr.journal.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.concurrent.Callable;

public final class JsonConverter {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        ParameterNamesModule module = new ParameterNamesModule();
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext context) throws IOException, JsonProcessingException {
                String dateStr = context.readValue(p, String.class);
                return ConversionUtils.convert(dateStr, LocalDate.class);
            }
        });
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    String dateStr = ConversionUtils.convert(value);
                    gen.writeString(dateStr);
                } else {
                    gen.writeNull();
                }
            }
        });

        OBJECT_MAPPER.registerModule(module);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        PrettyPrinter prettyPrinter = new CustomMinimalPrettyPrinter();
        OBJECT_MAPPER.setDefaultPrettyPrinter(prettyPrinter);
    }

    public static <T> void writeValue(OutputStream outputStream, T object) {
        processAndWrapCheckedException(() -> {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(outputStream, object);
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
