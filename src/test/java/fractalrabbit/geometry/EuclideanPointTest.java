package fractalrabbit.geometry;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.geometry.EuclideanPoint
 */
class EuclideanPointTest {

    @Test
    void distanceOfAThreeFourFiveTriangleIsFive() {
        EuclideanPoint origin = new EuclideanPoint(new double[]{0.0, 0.0});
        EuclideanPoint point = new EuclideanPoint(new double[]{3.0, 4.0});

        assertEquals(5.0, origin.distanceTo(point), 1e-12);
        assertEquals(origin.distanceTo(point), point.distanceTo(origin), 0.0);
    }

    @Test
    void identicalCoordinatesAreNearlyEqual() {
        EuclideanPoint point = new EuclideanPoint(new double[]{0.5, -0.25, 1.0});
        EuclideanPoint copy = new EuclideanPoint(new double[]{0.5, -0.25, 1.0});

        assertTrue(point.nearlyEquals(copy));
    }

    @Test
    void aDifferentMetricTypeIsNotNear() {
        EuclideanPoint point = new EuclideanPoint(new double[]{0.0});
        Metrizable other = new Metrizable() {
            @Override
            public double distanceTo(Metrizable ignored) {
                return 0.0;
            }

            @Override
            public boolean nearlyEquals(Metrizable ignored) {
                return true;
            }
        };

        assertFalse(point.nearlyEquals(other));
    }

    static void main() {
        Random g = new Random();
        int d = 3;
        /*
         * Two random points in [-1,1)^d
         */
        double[] x = g.doubles(d, -1.0, 1.0).toArray();
        EuclideanPoint px = new EuclideanPoint(x);
        double[] y = g.doubles(d, -1.0, 1.0).toArray();
        EuclideanPoint py = new EuclideanPoint(y);
        System.out.println("Distance from x to y: " + px.distanceTo(py));
        System.out.println("Are x and y equal? " + px.nearlyEquals(py));
        /*
         * Corrupt vector x by sqrt then square
         */
        double[] xx = new double[d];
        Arrays.setAll(xx, j -> Math.pow(Math.sqrt(x[j]), 2.0));
        EuclideanPoint pxx = new EuclideanPoint(xx);
        System.out.println("Are x and xx equal? " + px.nearlyEquals(pxx));
    }

}
