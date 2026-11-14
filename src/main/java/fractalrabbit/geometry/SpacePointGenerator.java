package fractalrabbit.geometry;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleBiFunction;

/**
 * Uniform samples from the Euclidean unit ball and its scalings.
 *
 * @author rwdarli
 */
public class SpacePointGenerator {
    private final int dimension;
    private final double[] origin;
    private final Random random = new Random();
    public final ToDoubleBiFunction<double[], double[]> distance = SpacePointGenerator::euclideanDistance;
    public final BiPredicate<double[], double[]> equalPoints = SpacePointGenerator::pointsAreEqual;

    /**
     * @param dimension ambient Euclidean dimension
     */
    public SpacePointGenerator(int dimension) {
        this.dimension = dimension;
        origin = new double[dimension];
    }

    /**
     * The shared zero vector. Callers must not mutate it.
     */
    public double[] zeroVector() {
        return origin;
    }

    /**
     * Rejection sample from the unit ball: uniform in {@code (-1, 1)^d} until the point lies inside the sphere.
     */
    public double[] samplePointUnitDisk() {
        double[] point = new double[dimension];
        boolean outside = true;
        while (outside) {
            Arrays.setAll(point, index -> -1.0 + 2.0 * random.nextDouble());
            outside = sumSquares(point) > 1.0;
        }
        return point;
    }

    /**
     * @param radius ball radius
     * @param center center, length {@code dimension}
     * @return uniform point in the ball of the given radius
     */
    public double[] sampleNearbyPoint(double radius, double[] center) {
        double[] unit = samplePointUnitDisk();
        double[] point = new double[dimension];
        Arrays.setAll(point, index -> center[index] + radius * unit[index]);
        return point;
    }

    /**
     * @return squared Euclidean norm
     */
    public double sumSquares(double[] vector) {
        double sum = 0.0;
        for (double coordinate : vector) {
            sum += coordinate * coordinate;
        }
        return sum;
    }

    /**
     * @return Euclidean distance, or {@link Double#NaN} when the lengths differ
     */
    public static double euclideanDistance(double[] left, double[] right) {
        if (left.length != right.length) {
            return Double.NaN;
        }
        double sumOfSquares = 0.0;
        for (int index = 0; index < left.length; index++) {
            double delta = left[index] - right[index];
            sumOfSquares += delta * delta;
        }
        return Math.sqrt(sumOfSquares);
    }

    /**
     * @return whether the points are at most one ulp apart and have the same length
     */
    public static boolean pointsAreEqual(double[] left, double[] right) {
        return left.length == right.length && euclideanDistance(left, right) <= Math.ulp(1.0);
    }
}
