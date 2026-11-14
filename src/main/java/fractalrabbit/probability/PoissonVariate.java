package fractalrabbit.probability;

import java.util.Random;

/**
 * Poisson variate via exponential interarrival times.
 *
 * @author rwdarli
 */
public class PoissonVariate {
    private final double lambda;
    private final Random random = new Random();

    /**
     * @param lambda mean of the Poisson distribution
     */
    public PoissonVariate(double lambda) {
        this.lambda = lambda;
    }

    /**
     * @return one draw from the generator
     */
    public int generate() {
        int count = 0;
        double sum = 0.0;
        while (sum < lambda) {
            sum -= Math.log(random.nextDouble());
            count++;
        }
        return count;
    }
}
