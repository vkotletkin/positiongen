package fractalrabbit.probability;

import org.apache.commons.statistics.distribution.BetaDistribution;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.simulation.SporadicReporter
 */
class BetaInverseCDFTest {

    private final BetaDistribution dist;

    public BetaInverseCDFTest() {
        dist = BetaDistribution.of(4.17, 2.98);
    }

    @Test
    void seasonalAdjustmentKeepsTheDayAndRescalesTheFraction() {
        BetaInverseCDFTest seasonal = new BetaInverseCDFTest();
        double fraction = seasonal.dist.inverseCumulativeProbability(0.25);
        double adjusted = Math.floor(3.25) + fraction;

        assertTrue(fraction > 0.0 && fraction < 1.0);
        assertEquals(3.0, Math.floor(adjusted), 0.0);
        assertTrue(adjusted < 4.0);
    }

    static void main() {
        BetaInverseCDFTest test = new BetaInverseCDFTest();
        Random g = new Random();
        DoubleUnaryOperator adjust = x -> Math.floor(x) + test.dist.inverseCumulativeProbability(x - Math.floor(x));
        long n = 20;
        double interval = 10.0;
        // Sorted
        Double[] t = g.doubles(n, 0.0, interval).boxed().sorted().toArray(Double[]::new);
        Double[] tseas = Arrays.stream(t).map(adjust::applyAsDouble).toArray(Double[]::new);
        System.out.println("Times with seasonal adjustments");
        for (int i = 0; i < n; i++) {
            System.out.println(t[i] + ", " + tseas[i]);
        }

    }

}
