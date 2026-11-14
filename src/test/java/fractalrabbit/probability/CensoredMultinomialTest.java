package fractalrabbit.probability;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.CensoredMultinomialTrial
 */
class CensoredMultinomialTest {

    private final CensoredMultinomialTrial cmt;

    public CensoredMultinomialTest() {
        cmt = new CensoredMultinomialTrial(51);
    }

    @Test
    void sampleReturnsTheOnlyAvailableCategory() {
        CensoredMultinomialTrial sampler = new CensoredMultinomialTrial(3);
        double[] rate = {1.0, 4.0, 9.0};
        boolean[] unavailable = {true, false, true};

        for (int draw = 0; draw < 20; draw++) {
            assertEquals(1, sampler.sample(rate, unavailable));
        }
    }

    @Test
    void sampleRejectsArraysOfTheWrongLength() {
        CensoredMultinomialTrial trial = new CensoredMultinomialTrial(3);

        assertThrows(IllegalArgumentException.class,
                () -> trial.sample(new double[1], new boolean[1]));
    }

    static void main() {
        final int n = 51;
        CensoredMultinomialTest test = new CensoredMultinomialTest();
        double[] rate = new double[n];
        /*
         * in this test the final rate component equals all the others put together
         */
        Arrays.setAll(rate, i -> (i < 50) ? 1.0 : 50.0);
        boolean[] offrange = new boolean[n];
        Arrays.fill(offrange, false);
        int s = 30;
        System.out.println("Draw " + s + " samples from distribution on {0, 1, ..., " + (n - 1) + "}");
        for (int t = 0; t < s; t++) {
            System.out.print(test.cmt.sample(rate, offrange) + "; ");
        }
        System.out.println();
        System.out.println("Same where " + (n - 1) + " is unavailable.");
        offrange[n - 1] = true;
        for (int t = 0; t < s; t++) {
            System.out.print(test.cmt.sample(rate, offrange) + "; ");
        }
        System.out.println();
        System.out.println("Same where " + (n - 1) + "  and states 0 to 9 are unavailable.");
        Arrays.fill(offrange, 0, 10, true);
        for (int t = 0; t < s; t++) {
            System.out.print(test.cmt.sample(rate, offrange) + "; ");
        }
    }

}
