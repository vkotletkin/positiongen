package fractalrabbit.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used only for development
 *
 * @author rwdarli
 * @see fractalrabbit.geometry.SpacePointGenerator
 */
class SpacePointTest {


    SpacePointGenerator x;

    public SpacePointTest() {
        x = new SpacePointGenerator(3);
    }

    @Test
    void unitDiskSampleLiesInsideTheSphere() {
        SpacePointGenerator generator = new SpacePointGenerator(3);
        double[] point = generator.samplePointUnitDisk();

        assertTrue(generator.sumSquares(point) <= 1.0);
    }

    @Test
    void nearbyPointStaysInsideTheRequestedRadius() {
        SpacePointGenerator generator = new SpacePointGenerator(2);
        double[] center = {0.2, -0.4};
        double radius = 0.07;
        double[] nearby = generator.sampleNearbyPoint(radius, center);

        assertTrue(SpacePointGenerator.euclideanDistance(center, nearby) <= radius + 1e-9);
    }

    @Test
    void identicalPointsMatchAndDifferentLengthsDoNot() {
        double[] point = {0.1, 0.2, 0.3};

        assertTrue(SpacePointGenerator.pointsAreEqual(point, point.clone()));
        assertFalse(SpacePointGenerator.pointsAreEqual(point, new double[]{0.1, 0.2}));
        assertTrue(new SpacePointGenerator(3).equalPoints.test(point, point.clone()));
    }

    static void main() {
        SpacePointTest spt = new SpacePointTest();
        System.out.println("Random point v1 in disc: ");
        double[] v1 = spt.x.samplePointUnitDisk();
        for (double z : v1) {
            System.out.print("," + z);
        }
        System.out.println();
        double[] v2 = spt.x.samplePointUnitDisk();
        System.out.println("Distance between two random points in disc: " + SpacePointGenerator.euclideanDistance(v1, v2));
        System.out.println("Test ToDoubleBifunction version: " + spt.x.distance.applyAsDouble(v1, v2));
        System.out.println("Are v1 and v2 equal? " + SpacePointGenerator.pointsAreEqual(v1, v2));
        System.out.println("Test BiPredicate version: " + spt.x.equalPoints.test(v1, v2));
        System.out.println("Are v1 and v1 equal? " + SpacePointGenerator.pointsAreEqual(v1, v1));
        System.out.println("Test BiPredicate version: " + spt.x.equalPoints.test(v1, v1));

        double radius = 0.07;
        double[] v3 = spt.x.sampleNearbyPoint(radius, v1);
        System.out.println("Random point v3 within radius " + radius + " of v1: ");
        for (double z : v3) {
            System.out.print("," + z);
        }
        System.out.println();
        System.out.println("Distance between v1 and v3: " + SpacePointGenerator.euclideanDistance(v1, v3));

    }

}
