package fractalrabbit.probability;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Normalized partial sums of i.i.d. truncated Pareto random variables on {@code (delta, 1]}.
 * Suggested parameters: {@code delta = 0.001}, {@code alpha = -1.5}.
 *
 * @author rwdarli
 */
public class HeavyTailedPartialSums {
    private final Random random = new Random();

    /**
     * @param size  length of the returned sequence
     * @param delta lower cutoff, greater than zero. The upper cutoff is 1.
     * @param alpha tail exponent, less than minus one
     * @return {@code (Y1 + ... + Yr) / (Y1 + ... + Y_{size+1})} for {@code r = 1..size}
     */
    public List<Double> generate(int size, double delta, double alpha) {
        double u1 = Math.pow(delta, 1.0 / alpha);
        double[] partialSums = random.doubles((long) size + 1)
                .map(uniform -> Math.pow(1.0 + (u1 - 1.0) * uniform, alpha))
                .toArray();
        Arrays.parallelPrefix(partialSums, Double::sum);
        double total = partialSums[size];
        return Arrays.stream(partialSums).limit(size).boxed().map(value -> value / total).toList();
    }
}
