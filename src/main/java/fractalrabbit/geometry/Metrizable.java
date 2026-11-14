package fractalrabbit.geometry;

/**
 * Finite metric used as the state space of a retro-preferential process.
 *
 * @author rwdarli
 * @see fractalrabbit.simulation.Retropreferential
 */
public interface Metrizable {
    double distanceTo(Metrizable other);

    /**
     * @return whether {@code other} lies less than one ulp from this point
     */
    boolean nearlyEquals(Metrizable other);
}
