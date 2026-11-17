package ru.kotletkin.positiongen.simulation;

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
 * Runs the FRACTALRABBIT generators in the same order as
 * {@link fractalrabbit.MainClassCyberBadger}. Model parameters come from the
 * request. Uploaded places replace the generated set; routes and reports
 * still come from the generators. The generator classes themselves are not modified.
 */
@Component
public class FractalRabbitCalculator {

    private final Random random = new Random();

    private static TravelerAssignment assignTravelers(int numCoTravelers, int numTravelers) {
        Integer[] travelerIds = new Integer[numTravelers];
        Arrays.setAll(travelerIds, id -> id);
        List<Integer> shuffled = Arrays.asList(travelerIds);
        Collections.shuffle(shuffled);
        Map<Integer, Integer> trajectoryAssignment = new HashMap<>();
        List<Integer> coTravelerIds = new ArrayList<>();
        for (int index = 0; index < numCoTravelers; index++) {
            int travelerId = shuffled.get(index);
            trajectoryAssignment.put(travelerId, 0);
            coTravelerIds.add(travelerId);
        }
        for (int index = numCoTravelers; index < numTravelers; index++) {
            trajectoryAssignment.put(shuffled.get(index), index - numCoTravelers + 1);
        }
        coTravelerIds.sort(Integer::compareTo);
        return new TravelerAssignment(trajectoryAssignment, List.copyOf(coTravelerIds));
    }

    private static int writeCoordinateCsv(List<double[]> points,
                                          Map<Integer, List<Double>> reportTimes,
                                          Map<Integer, List<Integer>> reportPlaces,
                                          double kilometersPerUnit,
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
                    double x = kilometersPerUnit * points.get(place)[0];
                    double y = kilometersPerUnit * points.get(place)[1];
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
                "positiongen-fractalrabbit-",
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
    }

    public SimulationOutput run(SimulationRequest request) throws IOException {
        List<double[]> points = places(request);
        EuclideanPoint[] metricPoints = new EuclideanPoint[points.size()];
        Arrays.setAll(metricPoints, index -> new EuclideanPoint(points.get(index)));
        Retropreferential retropreferential = new Retropreferential(metricPoints, request.distanceExponent());
        SporadicReporter reporter = new SporadicReporter(
                metricPoints,
                request.maxKilometersPerHour(),
                request.kilometersPerUnit());

        PoissonVariate phiRandom = new PoissonVariate(request.phiMean());
        PoissonVariate stepsRandom = new PoissonVariate(request.stepsMean());
        TravelerAssignment assignment = assignTravelers(request.coTravelerCount(), request.travelerCount());
        Map<Integer, Integer> trajectoryAssignment = assignment.trajectories();

        int trajectoryCount = request.travelerCount() - request.coTravelerCount() + 1;
        List<List<Integer>> trajectories = new ArrayList<>();
        for (int index = 0; index < trajectoryCount; index++) {
            trajectories.add(retropreferential.trajectory(
                    phiRandom.generate(),
                    stepsRandom.generate(),
                    random.nextInt(points.size())));
        }

        PoissonVariate countRandom = new PoissonVariate(request.meanReportCount());
        Map<Integer, List<Double>> reportTimes = new HashMap<>();
        Map<Integer, List<Integer>> reportPlaces = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : trajectoryAssignment.entrySet()) {
            List<Integer> visits = trajectories.get(entry.getValue());
            reporter.embedTrajectory(visits);
            reporter.generateReports(
                    visits,
                    countRandom.generate(),
                    request.reportDelta(),
                    request.reportAlpha(),
                    request.days());
            reportTimes.put(entry.getKey(), List.copyOf(reporter.getReportTimes()));
            reportPlaces.put(entry.getKey(), List.copyOf(reporter.getReportPlaces()));
        }

        Path directory = createPrivateTempDirectory();
        Path xyCsv = directory.resolve("XY.csv");
        Path placesCsv = directory.resolve("PLACES.csv");
        try {
            int xyRows = writeCoordinateCsv(points, reportTimes, reportPlaces, request.kilometersPerUnit(), xyCsv);
            int placeRows = writePlacesCsv(reportTimes, reportPlaces, placesCsv);
            if (xyRows != placeRows) {
                throw new IllegalStateException("XY.csv and PLACES.csv have different row counts");
            }
            return new SimulationOutput(directory, xyCsv, placesCsv, xyRows, assignment.coTravelerIds());
        } catch (IOException | RuntimeException ex) {
            deleteQuietly(xyCsv);
            deleteQuietly(placesCsv);
            deleteQuietly(directory);
            throw ex;
        }
    }

    private static List<double[]> places(final SimulationRequest request) {
        if (request.places() != null) {
            return Arrays.asList(request.places());
        }
        AgoraphobicPoints generated = new AgoraphobicPoints(
                request.pointCount(),
                request.dimension(),
                request.fractalDimension(),
                request.restartRate());
        generated.generatePoints();
        return generated.getPoints();
    }

    private record TravelerAssignment(Map<Integer, Integer> trajectories, List<Integer> coTravelerIds) {
    }
}
