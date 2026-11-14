/* THIS PUBLIC DOMAIN SOFTWARE WAS PRODUCED BY AN EMPLOYEE OF U.S. GOVERNMENT
 * AS PART OF THEIR OFFICIAL DUTIES.
 */
package fractalrabbit;

import fractalrabbit.geometry.EuclideanPoint;
import fractalrabbit.probability.PoissonVariate;
import fractalrabbit.simulation.AgoraphobicPoints;
import fractalrabbit.simulation.Retropreferential;
import fractalrabbit.simulation.SporadicReporter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Runs the three FRACTALRABBIT generators and writes synthetic waypoints to CSV.
 * <p>
 * Reference: R. W. R. Darling, "Retro-preferential Stochastic Mobility Models
 * on Random Fractals Under Sporadic Observations", 2018.
 *
 * @author rwdarli
 */
public class MainClassCyberBadger {
    private static final Logger LOGGER = Logger.getLogger(MainClassCyberBadger.class.getName());
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    /**
     * Restart rate for the agoraphobic point process.
     */
    private static final double RESTART_RATE = 0.75;
    /**
     * Distance-to-rate exponent for the retro-preferential process.
     */
    private static final double DISTANCE_EXPONENT = -2.0;
    /**
     * Traveler speed bound, kilometers per hour.
     */
    private static final double MAX_KILOMETERS_PER_HOUR = 50.0;
    /**
     * Kilometers represented by one abstract distance unit.
     */
    private static final double KILOMETERS_PER_UNIT = 200.0;
    /**
     * Mean of the Poisson exploration parameter.
     */
    private static final double PHI_MEAN = 10.0;
    /**
     * Mean trajectory length.
     */
    private static final double STEPS_MEAN = 100.0;
    /**
     * Lower cutoff for truncated Pareto inter-report gaps.
     */
    private static final double REPORT_DELTA = 0.001;
    /**
     * Tail exponent for truncated Pareto inter-report gaps.
     */
    private static final double REPORT_ALPHA = -1.5;

    private final AgoraphobicPoints points;
    private final Retropreferential retropreferential;
    private final SporadicReporter reporter;
    private final List<List<Integer>> trajectories = new ArrayList<>();
    private final List<Integer> coTravellers = new ArrayList<>();
    private final Map<Integer, Integer> trajectoryAssignment = new HashMap<>();
    private final Map<Integer, List<Double>> reportTimes = new HashMap<>();
    private final Map<Integer, List<Integer>> reportPlaces = new HashMap<>();

    /**
     * @param n            number of agoraphobic points
     * @param d            ambient Euclidean dimension
     * @param h            fractal dimension, less than {@code d}
     * @param theta        rate at which additional clumps form
     * @param exponent     negative exponent converting distances to transition rates
     * @param maxKmPerHour speed bound for travelers
     * @param kmPerUnit    kilometers per abstract distance unit
     */
    public MainClassCyberBadger(int n, int d, double h, double theta, double exponent, double maxKmPerHour, double kmPerUnit) {
        points = new AgoraphobicPoints(n, d, h, theta);
        points.generatePoints();
        EuclideanPoint[] metricPoints = new EuclideanPoint[points.getPoints().size()];
        Arrays.setAll(metricPoints, index -> new EuclideanPoint(points.getPoints().get(index)));
        retropreferential = new Retropreferential(metricPoints, exponent);
        reporter = new SporadicReporter(metricPoints, maxKmPerHour, kmPerUnit);
    }

    /**
     * Reads {@code parameters.csv} and writes {@code <output>XY.csv} and {@code <output>PLACES.csv}.
     * Each row is a traveler id, a time in days, and either planar coordinates or a place id.
     */
    static void main(String[] args) {
        try {
            List<String> values = readParameterValues(args[0]);
            Parameters parameters = Parameters.parse(values);
            String outputPath = args[1];

            MainClassCyberBadger simulator = new MainClassCyberBadger(
                    parameters.pointCount(),
                    parameters.dimension(),
                    parameters.fractalDimension(),
                    RESTART_RATE,
                    DISTANCE_EXPONENT,
                    MAX_KILOMETERS_PER_HOUR,
                    KILOMETERS_PER_UNIT);
            LOGGER.info(() -> parameters.pointCount() + " points generated for fractal dimension "
                    + parameters.fractalDimension());

            // These generators are created before the shuffle so each Random is seeded in the original order.
            PoissonVariate phiRandom = new PoissonVariate(PHI_MEAN);
            PoissonVariate stepsRandom = new PoissonVariate(STEPS_MEAN);
            Random startPoints = new Random();
            simulator.travelerConfigure(parameters.coTravelerCount(), parameters.travelerCount());
            int trajectoryCount = parameters.travelerCount() - parameters.coTravelerCount() + 1;
            simulator.addTrajectories(trajectoryCount, parameters.pointCount(), phiRandom, stepsRandom, startPoints);

            PoissonVariate countRandom = new PoissonVariate(parameters.meanReportCount());
            simulator.printCoTravellers(trajectoryCount);
            simulator.assignReports(countRandom, parameters.days());
            simulator.printFirstReports(parameters.travelerCount());

            writeCoordinateCsvHandlingFailure(simulator, outputPath);
            simulator.writePlacesCsv(outputPath);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not find parameter file.", ex);
        }
    }

    private static void writeCoordinateCsvHandlingFailure(MainClassCyberBadger simulator, String outputPath) {
        try {
            simulator.writeCoordinateCsv(outputPath);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not write coordinate file.", ex);
        }
    }

    private static List<String> readParameterValues(String path) throws IOException {
        List<String> values = new ArrayList<>();
        try (Reader reader = new FileReader(path)) {
            for (CSVRecord csvRecord : CSVFormat.RFC4180.parse(reader)) {
                LOGGER.info(() -> csvRecord.get(0) + " has value " + csvRecord.get(1));
                values.add(WHITESPACE.matcher(csvRecord.get(1)).replaceAll(""));
            }
        }
        return values;
    }

    /**
     * Assigns travelers to trajectories. The first {@code numCoTravelers} ids in a
     * random shuffle share trajectory 0; everyone else gets a distinct trajectory.
     */
    public void travelerConfigure(int numCoTravelers, int numTravelers) {
        Integer[] travelerIds = new Integer[numTravelers];
        Arrays.setAll(travelerIds, id -> id);
        List<Integer> shuffled = Arrays.asList(travelerIds);
        Collections.shuffle(shuffled);
        for (int index = 0; index < numCoTravelers; index++) {
            trajectoryAssignment.put(shuffled.get(index), 0);
            coTravellers.add(shuffled.get(index));
        }
        for (int index = numCoTravelers; index < numTravelers; index++) {
            trajectoryAssignment.put(shuffled.get(index), index - numCoTravelers + 1);
        }
    }

    private void addTrajectories(int trajectoryCount, int pointCount, PoissonVariate phiRandom,
                                 PoissonVariate stepsRandom, Random startPoints) {
        for (int index = 0; index < trajectoryCount; index++) {
            trajectories.add(retropreferential.trajectory(
                    phiRandom.generate(), stepsRandom.generate(), startPoints.nextInt(pointCount)));
        }
    }

    private void printCoTravellers(int trajectoryCount) {
        LOGGER.info(() -> trajectoryCount
                + " trajectories generated, in which travelers assigned to same trajectory are: "
                + coTravellers);
    }

    private void assignReports(PoissonVariate countRandom, double days) {
        for (Map.Entry<Integer, Integer> assignment : trajectoryAssignment.entrySet()) {
            List<Integer> visits = trajectories.get(assignment.getValue());
            reporter.embedTrajectory(visits);
            reporter.generateReports(visits, countRandom.generate(), REPORT_DELTA, REPORT_ALPHA, days);
            reportTimes.put(assignment.getKey(), List.copyOf(reporter.getReportTimes()));
            reportPlaces.put(assignment.getKey(), List.copyOf(reporter.getReportPlaces()));
        }
    }

    private void printFirstReports(int travelerCount) {
        for (Map.Entry<Integer, List<Double>> report : reportTimes.entrySet()) {
            Integer traveler = report.getKey();
            LOGGER.info(() -> "First report for traveler " + traveler + " at time "
                    + report.getValue().getFirst() + " is place " + reportPlaces.get(traveler).getFirst());
        }
        LOGGER.info(() -> "Reports have been generated for " + travelerCount + " travelers.");
    }

    private void writeCoordinateCsv(String outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputPath + "XY.csv"));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID", "Days", "x(km)", "y(km)");
            int lines = 0;
            for (Map.Entry<Integer, List<Double>> report : reportTimes.entrySet()) {
                Integer traveler = report.getKey();
                List<Double> times = report.getValue();
                List<Integer> places = reportPlaces.get(traveler);
                for (int index = 0; index < times.size(); index++) {
                    double time = times.get(index);
                    int place = places.get(index);
                    if (index == 0) {
                        LOGGER.info(() -> "First report for traveler " + traveler + " is point "
                                + place + " at time " + time);
                    }
                    double x = KILOMETERS_PER_UNIT * points.getPoints().get(place)[0];
                    double y = KILOMETERS_PER_UNIT * points.getPoints().get(place)[1];
                    csvPrinter.printRecord(traveler, time, x, y);
                    lines++;
                }
            }
            int writtenLines = lines;
            LOGGER.info(() -> "CSV file created with " + writtenLines + " lines, called " + outputPath + "XY.csv");
            csvPrinter.flush();
            writer.flush();
        }
    }

    private void writePlacesCsv(String outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputPath + "PLACES.csv"));
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
            int writtenLines = lines;
            LOGGER.info(() -> "CSV Places file created with " + writtenLines + " lines, called " + outputPath + "PLACES.csv");
            csvPrinter.flush();
            writer.flush();
        }
    }

    private record Parameters(int dimension, double fractalDimension, int pointCount, int travelerCount,
                              int coTravelerCount, double days, double meanReportCount) {
        static Parameters parse(List<String> values) {
            return new Parameters(
                    Integer.parseInt(values.get(0)),
                    Double.parseDouble(values.get(1)),
                    Integer.parseInt(values.get(2)),
                    Integer.parseInt(values.get(3)),
                    Integer.parseInt(values.get(4)),
                    Double.parseDouble(values.get(5)),
                    Double.parseDouble(values.get(6)));
        }
    }
}
