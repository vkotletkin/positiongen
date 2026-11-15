package ru.kotletkin.positiongen.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record SimulationOutput(
        Path directory,
        Path xyCsv,
        Path placesCsv,
        int reportCount,
        List<Integer> coTravelerIds
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(xyCsv);
        Files.deleteIfExists(placesCsv);
        Files.deleteIfExists(directory);
    }
}
