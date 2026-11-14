package fractalrabbit.probability;

import fractalrabbit.plot.ScatterPlotInputData;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.HeavyTailedPartialSums
 */
class HeavyTailedPartialSumsTest {
    private final HeavyTailedPartialSums ps;

    public HeavyTailedPartialSumsTest() {
        ps = new HeavyTailedPartialSums();
    }

    @Test
    void partialSumsIncreaseAndStayBelowOne() {
        List<Double> sums = new HeavyTailedPartialSums().generate(50, 0.001, -1.5);

        assertEquals(50, sums.size());
        assertTrue(sums.get(0) > 0.0);
        assertTrue(sums.get(sums.size() - 1) < 1.0);
        for (int index = 1; index < sums.size(); index++) {
            assertTrue(sums.get(index) >= sums.get(index - 1));
        }
    }

    static void main() {
        final int count = 1000;
        // Take delta = 0.001 (about 1/30 divided by 36 hours), alpha = -1.5
        final double delta = 0.001; // two minutes / 1.5 days
        final double alpha = -1.5;
        final double days = 90.0;

        HeavyTailedPartialSumsTest test = new HeavyTailedPartialSumsTest();

        Double[] xx = test.ps.generate(count, delta, alpha).toArray(new Double[count]);
        System.out.println("first entry: " + xx[0]);
        System.out.println("last entry: " + xx[count - 1]);

        // Create Scatter Plot
        double[] t = new double[xx.length];
        Arrays.setAll(t, j -> 1.0 + j); //1,2,3, ..., n
        double[] x = new double[count];
        Arrays.parallelSetAll(x, j -> days * xx[j].doubleValue()); //unboxing

        ScatterPlotInputData scatter = new ScatterPlotInputData("Point Process " + (-alpha), x, t);
        XYChart chartAPP = scatter.getChart();
        new SwingWrapper<XYChart>(chartAPP).displayChart();

    }

}
