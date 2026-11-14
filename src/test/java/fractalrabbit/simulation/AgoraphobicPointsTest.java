package fractalrabbit.simulation;
/*
 * Tests AgoraphobicPoints and displays 2-d graphic of Agoraphobic Point Process
 * Passed April 4, 2018.
 */

import fractalrabbit.plot.ScatterPlotInputData;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Used only for development
 *
 * @see fractalrabbit.simulation.AgoraphobicPoints
 */
class AgoraphobicPointsTest {

    private final int dimension;

    public AgoraphobicPointsTest() {
        dimension = 2;
    }

    @Test
    void generatePointsBuildsARootedTree() {
        AgoraphobicPoints points = new AgoraphobicPoints(25, 2, 1.33, 0.75);
        points.generatePoints();

        assertEquals(25, points.getPoints().size());
        assertArrayEquals(new double[]{0.0, 0.0}, points.getPoints().get(0), 0.0);
        assertEquals(25, points.getRootedTree().nodes().size());
        assertEquals(24, points.getRootedTree().edges().size());
        assertTrue(points.getSamplingEfficiency() > 0.0);
        assertTrue(points.getSamplingEfficiency() <= 1.0);
    }

    /*
     * Graphics
     */
    static void main() {
        int n = 100;
        double h = 1.33;
        double theta = 0.75;
        AgoraphobicPointsTest apt = new AgoraphobicPointsTest();
        AgoraphobicPoints app = new AgoraphobicPoints(n, apt.dimension, h, theta);
        app.generatePoints();
        double[] x = new double[n];
        Arrays.setAll(x, j -> app.getPoints().get(j)[0]);
        double[] y = new double[n];
        Arrays.setAll(y, j -> app.getPoints().get(j)[1]);
        // Create Scatter Plot
        ScatterPlotInputData scatter = new ScatterPlotInputData("Agoraphobic " + h, x, y);
        XYChart chartAPP = scatter.getChart();
        new SwingWrapper<XYChart>(chartAPP).displayChart();
        System.out.println("Sampling efficiency = " + app.getSamplingEfficiency() + " after " + n + " points.");

    }

}
