package fractalrabbit.probability;

import java.util.Arrays;
import java.util.Random;

/**
 * Multinomial draw on {@code {0, ..., n-1}} that skips categories marked unavailable.
 * In the retro-preferential process the unavailable categories are the places already visited.
 *
 * @author rwdarli
 */
public class CensoredMultinomialTrial {
    private final Random random = new Random();
    private final int dimension;
    private final double[] weights;

    /**
     * @param categoryCount number of categories
     */
    public CensoredMultinomialTrial(int categoryCount) {
        dimension = categoryCount;
        weights = new double[categoryCount];
    }

    /**
     * @param rates       non-negative unnormalized rates, length {@code categoryCount}
     * @param unavailable {@code unavailable[j]} means category {@code j} cannot be drawn
     * @return a category index
     * @throws IllegalArgumentException when the array lengths differ from the constructor size
     */
    public int sample(double[] rates, boolean[] unavailable) {
        if (rates.length != dimension || unavailable.length != dimension) {
            throw new IllegalArgumentException(" Dimensions do not match");
        }
        Arrays.setAll(weights, index -> unavailable[index] ? 0.0 : rates[index]);
        double rateSum = Arrays.stream(weights).sum();
        double threshold = rateSum * random.nextDouble();
        int index = 0;
        double cumulative = weights[0];
        while (cumulative < threshold) {
            index++;
            cumulative += weights[index];
        }
        return index;
    }
}
