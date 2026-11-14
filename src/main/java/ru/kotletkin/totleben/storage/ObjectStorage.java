package ru.kotletkin.totleben.storage;

import java.io.InputStream;
import java.nio.file.Path;

public interface ObjectStorage {

    void put(String key, Path file, String contentType);

    InputStream open(String key);
}
