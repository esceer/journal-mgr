package com.scr.journal.dao;

import com.scr.journal.util.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JsonLoader<T> implements DataLoader<T> {

    private final Path filePath;
    private final Class<T> valueType;

    public JsonLoader(Path jsonFilePath, Class<T> valueType) {
        this.filePath = jsonFilePath;
        this.valueType = valueType;
    }

    @Override
    public T load() {
        if (!Files.exists(filePath)) {
            return null;
        } else {
            try (InputStream inputStream = Files.newInputStream(filePath, StandardOpenOption.READ)) {
                return JsonConverter.readValue(inputStream, valueType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
