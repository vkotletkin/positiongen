package fractalrabbit.simulation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Development driver that writes an agoraphobic point set to CSV.
 *
 * @see AgoraphobicPoints
 */
public class AgoraphobicPointsWriter {
    final int dimension;

    public AgoraphobicPointsWriter(int dimension) {
        this.dimension = dimension;
    }

    static void main() {
        try {
            int pointCount = 100;
            int dimension = 2;
            double fractalDimension = 1.33;
            double theta = 0.75;
            String csvOutputFile = "fractaldim-" + fractalDimension + "-ambient-" + dimension + "-size-" + pointCount + ".csv";
            AgoraphobicPointsWriter writer = new AgoraphobicPointsWriter(dimension);
            AgoraphobicPoints points = new AgoraphobicPoints(pointCount, writer.dimension, fractalDimension, theta);
            points.generatePoints();
            System.out.println("Sampling efficiency = " + points.getSamplingEfficiency() + " after " + pointCount + " points.");

            try (BufferedWriter file = Files.newBufferedWriter(Path.of(csvOutputFile));
                 CSVPrinter csvPrinter = new CSVPrinter(file, CSVFormat.DEFAULT.withDelimiter(','))) {
                int lines = 0;
                for (double[] point : points.getPoints()) {
                    csvPrinter.printRecords(point);
                    lines++;
                }
                System.out.println("CSV file created with " + lines + " lines, called " + csvOutputFile + ".csv");
                csvPrinter.flush();
                file.flush();
            }
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Could not find parameter file.");
        }
    }
}
