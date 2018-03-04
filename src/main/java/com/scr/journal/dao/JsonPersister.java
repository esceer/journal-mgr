package com.scr.journal.dao;

import com.scr.journal.util.JsonConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class JsonPersister<T> implements DataPersister<T> {

    private final Path filePath;
    private final Path backUpFilePath;

    public JsonPersister(Path jsonFilePath, Path backUpFilePath) {
        this.filePath = jsonFilePath;
        this.backUpFilePath = backUpFilePath;
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

    @Override
    public void createBackup() {
        try {
            if (Files.exists(filePath)) {
                Files.copy(filePath, backUpFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
