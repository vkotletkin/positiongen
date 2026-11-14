package fractalrabbit.probability;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.ReweightedMultinomialTrial
 */
class ReweightedMultinomialTest {

    ReweightedMultinomialTrial rmt;

    public ReweightedMultinomialTest() {
        this.rmt = new ReweightedMultinomialTrial();
    }

    @Test
    void sampleStaysInsidePlacesWithPositiveWeight() {
        ReweightedMultinomialTrial trial = new ReweightedMultinomialTrial();
        Set<Integer> places = Set.of(1, 3);
        double[] rates = {0.0, 2.0, 0.0, 5.0};
        double[] frequencies = {0.0, 1.0, 0.0, 1.0};

        for (int draw = 0; draw < 20; draw++) {
            assertTrue(places.contains(trial.sample(places, rates, frequencies)));
        }
    }

    @Test
    void zeroWeightsReturnTheSentinel() {
        ReweightedMultinomialTrial trial = new ReweightedMultinomialTrial();

        assertEquals(Integer.MIN_VALUE, trial.sample(Set.of(0), new double[]{0.0}, new double[]{1.0}));
    }

    @Test
    void mismatchedArraysAreRejected() {
        ReweightedMultinomialTrial trial = new ReweightedMultinomialTrial();
        Set<Integer> places = Set.of(0);
        double[] rates = {1.0};
        double[] frequencies = {1.0, 2.0};

        assertThrows(IllegalArgumentException.class, () -> trial.sample(places, rates, frequencies));
    }

    static void main() {
        final int n = 60; // overall set of states
        final int s = 35; // sample size
        double[] rate = new double[n];
        double[] freq = new double[n];
        Random g = new Random();
        Arrays.setAll(rate, i -> g.nextDouble()); // random rates
        final double mu = 4.0;
        Arrays.setAll(freq, i -> Math.floor(mu * g.nextDouble())); // round a random double down
        /*
         * the places are states whose frequency is not zero
         */
        Set<Integer> places = new HashSet<Integer>();
        for (int j = 0; j < n; j++) {
            if (freq[j] > 0.0) {
                places.add(j);
            }
        }
        ReweightedMultinomialTest test = new ReweightedMultinomialTest();
        System.out.println("Draw " + s + " samples from distribution on " + places.size() + " integers.");
        for (int t = 0; t < s; t++) {
            System.out.print(test.rmt.sample(places, rate, freq) + "; ");
        }

    }

}
