package ru.kotletkin.totleben.result;

import ru.kotletkin.totleben.simulation.SimulationOutput;

import java.io.InputStream;
import java.util.UUID;

public interface ResultArchive {

    StoredArtifacts store(UUID calculationId, SimulationOutput output);

    StoredArtifacts files(UUID calculationId);

    InputStream open(String objectKey);
}
