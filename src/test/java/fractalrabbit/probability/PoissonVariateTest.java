package fractalrabbit.probability;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.PoissonVariate
 */
class PoissonVariateTest {

    private final PoissonVariate pv;

    public PoissonVariateTest() {
        pv = new PoissonVariate(1000.0);
    }

    @Test
    void drawsAreAtLeastOne() {
        PoissonVariate variate = new PoissonVariate(2.0);
        for (int trial = 0; trial < 30; trial++) {
            assertTrue(variate.generate() >= 1);
        }
    }

    static void main() {
        PoissonVariateTest test = new PoissonVariateTest();
        int ntrials = 100;
        int[] data = new int[ntrials];
        Arrays.setAll(data, i -> test.pv.generate());

        IntSummaryStatistics stats = Arrays.stream(data).summaryStatistics();
        System.out.println(ntrials + " trials: " + stats);

    }

}
