package fractalrabbit.geometry;

/**
 * Point in a Euclidean metric space. Near-equality is distance below one ulp.
 *
 * @author rwdarli
 */
public class EuclideanPoint implements Metrizable {
    private final double[] coordinates;
    private final int dimension;

    public EuclideanPoint(double[] point) {
        coordinates = point;
        dimension = point.length;
    }

    @Override
    public double distanceTo(Metrizable other) {
        if (other.getClass() != getClass()) {
            return Double.NaN;
        }
        double[] otherCoordinates = ((EuclideanPoint) other).coordinates;
        double sumOfSquares = 0.0;
        for (int index = 0; index < dimension; index++) {
            double delta = coordinates[index] - otherCoordinates[index];
            sumOfSquares += delta * delta;
        }
        return Math.sqrt(sumOfSquares);
    }

    @Override
    public boolean nearlyEquals(Metrizable other) {
        return distanceTo(other) < Math.ulp(1.0);
    }
}
