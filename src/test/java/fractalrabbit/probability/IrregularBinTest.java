package fractalrabbit.probability;

/**
 * Ran successfully 11.14.2018
 */

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.probability.IrregularBinAssignment
 */
class IrregularBinTest {

    private final IrregularBinAssignment<Double> iba;

    public IrregularBinTest() {
        iba = new IrregularBinAssignment<>();
    }

    @Test
    void observationsFallIntoHalfOpenBins() {
        IrregularBinAssignment<Double> bins = new IrregularBinAssignment<>();
        bins.assign(List.of(0.0, 1.0, 3.0, 6.0), List.of(0.2, 1.0, 2.5, 5.9));

        assertEquals(List.of(0, 1, 1, 2), bins.getPlacements());
    }

    static void main() {
        IrregularBinTest test = new IrregularBinTest();
        Random g = new Random();
        List<Double> obs = g.doubles(100).map(x -> -Math.log(x)).sorted().boxed().toList();
        double maxObs = obs.get(obs.size() - 1);
        List<Double> bins = new ArrayList<Double>();
        bins.add(0.0); // first element
        int bincount = 20; // add one for endpoints
        bins.addAll(g.doubles(bincount, 0.0, Math.ceil(maxObs)).sorted().boxed().toList());
        test.iba.assign(bins, obs);
        System.out.println("bin boundaries:");
        for (Double a : bins) {
            System.out.print(a + ", ");
        }
        System.out.println();
        int testbin = g.nextInt(bincount);

        int counter = 0;
        for (Double b : obs) {
            if ((b >= bins.get(testbin)) && (b < bins.get(testbin + 1))) {
                counter++;
            }
        }
        System.out.println("observation count for bin " + testbin + " = " + counter);
        System.out.println();
        System.out.println("placements of observations in bins:");
        for (Integer p : test.iba.getPlacements()) {
            System.out.print(p + ", ");
        }
    }

}
