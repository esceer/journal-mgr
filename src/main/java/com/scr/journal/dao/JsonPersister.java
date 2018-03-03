package com.scr.journal.dao;

import com.scr.journal.util.JsonConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JsonPersister<T> implements DataPersister<T> {

    private final Path filePath;

    public JsonPersister(Path jsonFilePath) {
        this.filePath = jsonFilePath;
    }

    @Override
    public void persist(T data) {
        try (OutputStream outputStream = Files.newOutputStream(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            JsonConverter.writeValue(outputStream, data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
