package ru.kotletkin.totleben.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SimulationOutput implements AutoCloseable {

    private final Path directory;
    private final Path xyCsv;
    private final Path placesCsv;
    private final int reportCount;

    SimulationOutput(Path directory, Path xyCsv, Path placesCsv, int reportCount) {
        this.directory = directory;
        this.xyCsv = xyCsv;
        this.placesCsv = placesCsv;
        this.reportCount = reportCount;
    }

    public Path xyCsv() {
        return xyCsv;
    }

    public Path placesCsv() {
        return placesCsv;
    }

    public int reportCount() {
        return reportCount;
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(xyCsv);
        Files.deleteIfExists(placesCsv);
        Files.deleteIfExists(directory);
    }
}
