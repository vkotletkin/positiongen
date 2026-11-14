package ru.kotletkin.totleben.simulation;

import fractalrabbit.geometry.EuclideanPoint;
import fractalrabbit.probability.PoissonVariate;
import fractalrabbit.simulation.AgoraphobicPoints;
import fractalrabbit.simulation.Retropreferential;
import fractalrabbit.simulation.SporadicReporter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * Runs the FRACTALRABBIT generators in the same order and with the same fixed
 * constants as {@link fractalrabbit.MainClassCyberBadger}. The generator classes
 * themselves are not modified.
 */
@Component
public class FractalRabbitCalculator {

    /**
     * Restart rate for the agoraphobic point process.
     */
    static final double RESTART_RATE = 0.75;
    /**
     * Distance-to-rate exponent for the retro-preferential process.
     */
    static final double DISTANCE_EXPONENT = -2.0;
    /**
     * Traveler speed bound, kilometers per hour.
     */
    static final double MAX_KILOMETERS_PER_HOUR = 50.0;
    /**
     * Kilometers represented by one abstract distance unit.
     */
    static final double KILOMETERS_PER_UNIT = 200.0;
    /**
     * Mean of the Poisson exploration parameter.
     */
    static final double PHI_MEAN = 10.0;
    /**
     * Mean trajectory length.
     */
    static final double STEPS_MEAN = 100.0;
    /**
     * Lower cutoff for truncated Pareto inter-report gaps.
     */
    static final double REPORT_DELTA = 0.001;
    /**
     * Tail exponent for truncated Pareto inter-report gaps.
     */
    static final double REPORT_ALPHA = -1.5;

    private final Random random = new Random();

    public FixedParameters fixedParameters() {
        return new FixedParameters(
                RESTART_RATE,
                DISTANCE_EXPONENT,
                MAX_KILOMETERS_PER_HOUR,
                KILOMETERS_PER_UNIT,
                PHI_MEAN,
                STEPS_MEAN,
                REPORT_DELTA,
                REPORT_ALPHA);
    }

    public SimulationOutput run(SimulationRequest request) throws IOException {
        AgoraphobicPoints points = new AgoraphobicPoints(
                request.pointCount(),
                request.dimension(),
                request.fractalDimension(),
                RESTART_RATE);
        points.generatePoints();
        EuclideanPoint[] metricPoints = new EuclideanPoint[points.getPoints().size()];
        Arrays.setAll(metricPoints, index -> new EuclideanPoint(points.getPoints().get(index)));
        Retropreferential retropreferential = new Retropreferential(metricPoints, DISTANCE_EXPONENT);
        SporadicReporter reporter = new SporadicReporter(metricPoints, MAX_KILOMETERS_PER_HOUR, KILOMETERS_PER_UNIT);

        PoissonVariate phiRandom = new PoissonVariate(PHI_MEAN);
        PoissonVariate stepsRandom = new PoissonVariate(STEPS_MEAN);
        Map<Integer, Integer> trajectoryAssignment = assignTravelers(request.coTravelerCount(), request.travelerCount());

        int trajectoryCount = request.travelerCount() - request.coTravelerCount() + 1;
        List<List<Integer>> trajectories = new ArrayList<>();
        for (int index = 0; index < trajectoryCount; index++) {
            trajectories.add(retropreferential.trajectory(
                    phiRandom.generate(),
                    stepsRandom.generate(),
                    random.nextInt(request.pointCount())));
        }

        PoissonVariate countRandom = new PoissonVariate(request.meanReportCount());
        Map<Integer, List<Double>> reportTimes = new HashMap<>();
        Map<Integer, List<Integer>> reportPlaces = new HashMap<>();
        for (Map.Entry<Integer, Integer> assignment : trajectoryAssignment.entrySet()) {
            List<Integer> visits = trajectories.get(assignment.getValue());
            reporter.embedTrajectory(visits);
            reporter.generateReports(
                    visits,
                    countRandom.generate(),
                    REPORT_DELTA,
                    REPORT_ALPHA,
                    request.days());
            reportTimes.put(assignment.getKey(), List.copyOf(reporter.getReportTimes()));
            reportPlaces.put(assignment.getKey(), List.copyOf(reporter.getReportPlaces()));
        }

        Path directory = createPrivateTempDirectory();
        Path xyCsv = directory.resolve("XY.csv");
        Path placesCsv = directory.resolve("PLACES.csv");
        try {
            int xyRows = writeCoordinateCsv(points, reportTimes, reportPlaces, xyCsv);
            int placeRows = writePlacesCsv(reportTimes, reportPlaces, placesCsv);
            if (xyRows != placeRows) {
                throw new IllegalStateException("XY.csv и PLACES.csv разошлись по числу строк");
            }
            return new SimulationOutput(directory, xyCsv, placesCsv, xyRows);
        } catch (IOException | RuntimeException ex) {
            deleteQuietly(xyCsv);
            deleteQuietly(placesCsv);
            deleteQuietly(directory);
            throw ex;
        }
    }

    private static Map<Integer, Integer> assignTravelers(int numCoTravelers, int numTravelers) {
        Integer[] travelerIds = new Integer[numTravelers];
        Arrays.setAll(travelerIds, id -> id);
        List<Integer> shuffled = Arrays.asList(travelerIds);
        Collections.shuffle(shuffled);
        Map<Integer, Integer> trajectoryAssignment = new HashMap<>();
        for (int index = 0; index < numCoTravelers; index++) {
            trajectoryAssignment.put(shuffled.get(index), 0);
        }
        for (int index = numCoTravelers; index < numTravelers; index++) {
            trajectoryAssignment.put(shuffled.get(index), index - numCoTravelers + 1);
        }
        return trajectoryAssignment;
    }

    private static int writeCoordinateCsv(AgoraphobicPoints points,
                                          Map<Integer, List<Double>> reportTimes,
                                          Map<Integer, List<Integer>> reportPlaces,
                                          Path output) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID", "Days", "x(km)", "y(km)");
            int lines = 0;
            for (Map.Entry<Integer, List<Double>> report : reportTimes.entrySet()) {
                Integer traveler = report.getKey();
                List<Double> times = report.getValue();
                List<Integer> places = reportPlaces.get(traveler);
                for (int index = 0; index < times.size(); index++) {
                    int place = places.get(index);
                    double x = KILOMETERS_PER_UNIT * points.getPoints().get(place)[0];
                    double y = KILOMETERS_PER_UNIT * points.getPoints().get(place)[1];
                    csvPrinter.printRecord(traveler, times.get(index), x, y);
                    lines++;
                }
            }
            csvPrinter.flush();
            return lines;
        }
    }

    private static int writePlacesCsv(Map<Integer, List<Double>> reportTimes,
                                      Map<Integer, List<Integer>> reportPlaces,
                                      Path output) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("Traveler ID", "Days", "Place ID");
            int lines = 0;
            for (Map.Entry<Integer, List<Double>> report : reportTimes.entrySet()) {
                Integer traveler = report.getKey();
                List<Double> times = report.getValue();
                List<Integer> places = reportPlaces.get(traveler);
                for (int index = 0; index < times.size(); index++) {
                    csvPrinter.printRecord(traveler, times.get(index), places.get(index));
                    lines++;
                }
            }
            csvPrinter.flush();
            return lines;
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException _) {
            // The calling failure is the one that should surface.
        }
    }

    private static Path createPrivateTempDirectory() throws IOException {
        return Files.createTempDirectory(
                "totleben-fractalrabbit-",
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
    }

    public record FixedParameters(
            double restartRate,
            double distanceExponent,
            double maxKilometersPerHour,
            double kilometersPerUnit,
            double phiMean,
            double stepsMean,
            double reportDelta,
            double reportAlpha
    ) {
    }
}
