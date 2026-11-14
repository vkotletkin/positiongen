/* THIS PUBLIC DOMAIN SOFTWARE WAS PRODUCED BY AN EMPLOYEE OF U.S. GOVERNMENT
 * AS PART OF THEIR OFFICIAL DUTIES.
 */
package fractalrabbit.simulation;

import fractalrabbit.geometry.Metrizable;
import fractalrabbit.probability.CensoredMultinomialTrial;
import fractalrabbit.probability.ReweightedMultinomialTrial;

import java.util.*;

/**
 * Classic retro-preferential process on a finite metric space.
 * Transition rates out of a point are computed the first time it is visited,
 * and revisit steps only sample among places already seen.
 *
 * @author rwdarli
 */
public class Retropreferential {
    private final int placeCount;
    private final Metrizable[] points;
    /**
     * Outgoing rates from each place, filled on first visit. Shared across trajectories.
     */
    private final Map<Integer, double[]> rates = new HashMap<>();
    private final double exponent;
    private final Set<Integer> placesVisited = new HashSet<>();
    private final boolean[] visited;
    private final double[] visitTally;
    private final ReweightedMultinomialTrial revisitor = new ReweightedMultinomialTrial();
    private final CensoredMultinomialTrial explorer;
    private final Random random;

    /**
     * @param pointsArray states of the process
     * @param exponent    negative exponent; {@code -2} is quadratic decay of distance
     */
    public Retropreferential(Metrizable[] pointsArray, double exponent) {
        points = pointsArray;
        placeCount = pointsArray.length;
        this.exponent = exponent;
        visited = new boolean[placeCount];
        visitTally = new double[placeCount];
        explorer = new CensoredMultinomialTrial(placeCount);
        random = new Random();
    }

    /**
     * Computes transition rates out of {@code place} the first time it is needed.
     * Self-rate is zero.
     */
    public void insertRate(int place) {
        double[] outgoing = new double[placeCount];
        Arrays.setAll(outgoing, other -> other == place
                ? 0.0
                : Math.pow(points[place].distanceTo(points[other]), exponent));
        rates.put(place, outgoing);
    }

    /**
     * One trajectory. Visit counters are reset; cached rates are kept.
     *
     * @param phi   exploration parameter of the pure-birth process
     * @param steps maximum number of states, including the start
     * @param start index of the starting place
     * @return place indices, in visit order
     */
    public List<Integer> trajectory(double phi, int steps, int start) {
        placesVisited.clear();
        Arrays.fill(visited, false);
        Arrays.setAll(visitTally, index -> index == start ? 1.0 : 0.0);

        int state = start;
        List<Integer> trajectory = new ArrayList<>();
        trajectory.add(state);
        if (!rates.containsKey(state)) {
            insertRate(state);
        }
        placesVisited.add(state);
        visited[state] = true;

        while (trajectory.size() < steps && placesVisited.size() < placeCount) {
            boolean explore = random.nextDouble() < phi / (phi + placesVisited.size() - 1.0);
            state = explore
                    ? explorer.sample(rates.get(state), visited)
                    : revisitor.sample(placesVisited, rates.get(state), visitTally);
            trajectory.add(state);
            visitTally[state]++;
            if (!rates.containsKey(state)) {
                insertRate(state);
            }
            if (explore) {
                placesVisited.add(state);
                visited[state] = true;
            }
        }
        return trajectory;
    }

    public double[] getVisitTally() {
        return visitTally;
    }

    public Set<Integer> getPlacesVisited() {
        return placesVisited;
    }

    public boolean[] getIsVisited() {
        return visited;
    }
}
