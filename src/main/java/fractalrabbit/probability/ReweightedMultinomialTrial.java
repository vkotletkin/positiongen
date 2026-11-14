package fractalrabbit.probability;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Draw from a small set of places, weighted by rate times visit frequency.
 * The walk is over the visited set only, which is what keeps a revisit step cheap.
 *
 * @author rwdarli
 * @see fractalrabbit.simulation.Retropreferential
 */
public class ReweightedMultinomialTrial {
    private final Random random = new Random();
    private final Map<Integer, Double> weights = new HashMap<>();

    /**
     * @param places      subset of {@code {0, ..., n-1}}
     * @param rates       unnormalized rates indexed by place
     * @param frequencies visit counts indexed by place
     * @return a place from {@code places}, or {@link Integer#MIN_VALUE} when every weight is zero
     * @throws IllegalArgumentException when {@code rates} and {@code frequencies} have different lengths
     */
    public Integer sample(Set<Integer> places, double[] rates, double[] frequencies) {
        if (rates.length != frequencies.length) {
            throw new IllegalArgumentException(" Dimensions do not match");
        }
        weights.clear();
        for (Integer place : places) {
            weights.put(place, frequencies[place] * rates[place]);
        }
        double rateSum = weights.values().stream().reduce(0.0, Double::sum);
        double threshold = rateSum * random.nextDouble();
        double cumulative = 0.0;
        Integer index = Integer.MIN_VALUE;
        for (Map.Entry<Integer, Double> weightedPlace : weights.entrySet()) {
            cumulative += weightedPlace.getValue();
            if (cumulative > threshold) {
                index = weightedPlace.getKey();
                break;
            }
        }
        return index;
    }
}
