package fractalrabbit.probability;

import fractalrabbit.plot.ScatterPlotInputData;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.HeavyTailPointProcess
 */
class HeavyTailPointProcessTest {
    private final HeavyTailPointProcess pp;

    public HeavyTailPointProcessTest() {
        pp = new HeavyTailPointProcess();
    }

    @Test
    void arrivalsStayInsideTheWindowAndIncrease() {
        double duration = 30.0;
        double[] arrivals = new HeavyTailPointProcess().generate(duration, 0.01, 1.0, -2.5);

        double previous = 0.0;
        for (double arrival : arrivals) {
            assertTrue(arrival >= previous);
            assertTrue(arrival < duration);
            previous = arrival;
        }
    }

    @Test
    void meanInterarrivalTimeIsPositive() {
        assertTrue(new HeavyTailPointProcess().meanRate(0.01, 1.5, -2.5) > 0.0);
    }

    static void main() {
        final double duration = 100.0; // days
        // Gives about 15 events / day with these choices.
        final double lo = 1.0 / (30.0 * 24.0); // two minutes in days
        final double hi = 1.5; // day & a half

        HeavyTailPointProcessTest test = new HeavyTailPointProcessTest();

        /*
         * Mean events per day
         */
        System.out.println("How does alphaDaily relate to average events per day, keeping lo and hi fixed?");
        int mesh = 30;
        double[] alphaValues = new double[mesh];
        Arrays.setAll(alphaValues, j -> -1.5 - j * 0.1);
        double[] meanValues = new double[mesh];
        Arrays.setAll(meanValues, j -> 1.0 / test.pp.meanRate(lo, hi, alphaValues[j]));
        for (int j = 0; j < mesh; j++) {
            System.out.print("{" + alphaValues[j] + ", " + meanValues[j] + "}, ");
        }
        System.out.println();

        final double alpha = -2.5; // tail parameter

        System.out.println("How does lo relate to average events per day, keeping alphaDaily and hi fixed?");
        double[] loValues = new double[mesh];
        double sec = 1.0 / (3600.0 * 24.0);
        Arrays.setAll(loValues, j -> (j + 1) * 4.0 * sec); // multiples of 4 seconds
        Arrays.setAll(meanValues, j -> 1.0 / test.pp.meanRate(loValues[j], hi, alpha));
        for (int j = 0; j < mesh; j++) {
            System.out.print("{" + (loValues[j] / sec) + ", " + meanValues[j] + "}, ");
        }
        System.out.println();

        double[] x = test.pp.generate(duration, lo, hi, alpha);
        System.out.println("# arrivals = " + x.length);
        System.out.println("Intended duration: " + duration + "; actual duration: " + x[x.length - 1]);
        /*
         * What rate did we expect?
         */
        double paretoMean = test.pp.meanRate(lo, hi, alpha);
        double actualRate = x.length / duration;
        System.out.println("Expected rate / day: " + (1.0 / paretoMean) + "; actual rate: " + actualRate);

        // Create Scatter Plot
        double[] y = new double[x.length];
        Arrays.setAll(y, j -> 1.0 + j); // 1,2,3, ..., n

        ScatterPlotInputData scatter = new ScatterPlotInputData("Point Process " + (-alpha), x, y);
        XYChart chartAPP = scatter.getChart();
        new SwingWrapper<XYChart>(chartAPP).displayChart();

    }

}
