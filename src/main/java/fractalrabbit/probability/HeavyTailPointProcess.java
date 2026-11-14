package fractalrabbit.probability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Point process with truncated Pareto interarrival times, {@code X = U^alpha} for {@code U} uniform on
 * {@code (u0, u1)} and {@code alpha < -1}.
 *
 * @author rwdarli
 */
public class HeavyTailPointProcess {
    private final Random random = new Random();

    /**
     * @param duration upper bound for the last retained arrival
     * @param lo       minimum interarrival time
     * @param hi       maximum interarrival time
     * @param alpha    tail exponent, less than minus one
     * @return arrival times. The draw that crosses {@code duration} is dropped.
     */
    public double[] generate(double duration, double lo, double hi, double alpha) {
        double u0 = Math.pow(hi, 1.0 / alpha);
        double u1 = Math.pow(lo, 1.0 / alpha);
        List<Double> arrivals = new ArrayList<>();
        double time = 0.0;
        while (time < duration) {
            time += Math.pow(u0 + (u1 - u0) * random.nextDouble(), alpha);
            arrivals.add(time);
        }
        double[] series = new double[arrivals.size() - 1];
        Arrays.setAll(series, arrivals::get);
        return series;
    }

    /**
     * Mean interarrival time of the truncated Pareto law used by {@link #generate}.
     * The mean event rate is the reciprocal of this value.
     */
    public double meanRate(double lo, double hi, double alpha) {
        double u0 = Math.pow(hi, 1.0 / alpha);
        double u1 = Math.pow(lo, 1.0 / alpha);
        double c = 1.0 + alpha;
        return (Math.pow(u0, c) - Math.pow(u1, c)) / ((-1.0 - alpha) * (u1 - u0));
    }
}
