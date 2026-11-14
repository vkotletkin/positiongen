package fractalrabbit.probability;

import java.util.ArrayList;
import java.util.List;

/**
 * Places sorted observations into half-open bins {@code [a[k], a[k+1])}.
 * Bin boundaries and observations must both be sorted, with every observation
 * strictly below the last boundary.
 *
 * @author rwdarli
 */
public class IrregularBinAssignment<T extends Comparable<? super T>> {
    private final List<Integer> placements = new ArrayList<>();

    /**
     * @param boundaries   ascending bin edges, including both endpoints
     * @param observations ascending values to place
     */
    public void assign(List<T> boundaries, List<T> observations) {
        placements.clear();
        int upperIndex = 1;
        T upper = boundaries.get(upperIndex);
        for (T observation : observations) {
            while (observation.compareTo(upper) >= 0) {
                upperIndex++;
                upper = boundaries.get(upperIndex);
            }
            placements.add(upperIndex - 1);
        }
    }

    /**
     * Bin index of each observation from the last {@link #assign} call.
     */
    public List<Integer> getPlacements() {
        return placements;
    }
}
