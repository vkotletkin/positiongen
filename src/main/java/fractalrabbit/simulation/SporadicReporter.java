/* THIS PUBLIC DOMAIN SOFTWARE WAS PRODUCED BY AN EMPLOYEE OF U.S. GOVERNMENT
 * AS PART OF THEIR OFFICIAL DUTIES.
 */
package fractalrabbit.simulation;

import fractalrabbit.geometry.Metrizable;
import fractalrabbit.probability.HeavyTailedPartialSums;
import fractalrabbit.probability.IrregularBinAssignment;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Embeds a visit sequence in continuous time, then emits bursty report times
 * with daily seasonality. Travel consumes time, so a report cannot fall inside
 * a transition that would break the speed bound.
 *
 * @author rwdarli
 */
public class SporadicReporter {

    private static final Logger LOGGER = Logger.getLogger(SporadicReporter.class.getName());
    private static final double DAILY_ALPHA = 4.17;
    private static final double DAILY_BETA = 2.98;

    private final Metrizable[] points;
    private final HeavyTailedPartialSums partialSums = new HeavyTailedPartialSums();
    private final IrregularBinAssignment<Double> bins = new IrregularBinAssignment<>();
    private final BetaDistribution dailySeasonality = new BetaDistribution(DAILY_ALPHA, DAILY_BETA);
    private final double speedBound;
    private final List<Double> uniformOrderStatistics = new ArrayList<>();
    private final List<Double> cumulativeTravelTime = new ArrayList<>();
    private final List<Double> reportTimes = new ArrayList<>();
    private final List<Integer> reportPlaces = new ArrayList<>();
    private final Random random = new Random();

    /**
     * @param pointsArray  metric space containing every place index
     * @param maxKmPerHour speed bound
     * @param kmPerUnit    kilometers per abstract distance unit
     */
    public SporadicReporter(Metrizable[] pointsArray, double maxKmPerHour, double kmPerUnit) {
        speedBound = 24.0 * maxKmPerHour / kmPerUnit;
        points = pointsArray;
    }

    /**
     * Builds travel-time bins for {@code visitSequence}. Later calls replace the previous embedding.
     */
    public void embedTrajectory(List<Integer> visitSequence) {
        int moves = visitSequence.size() - 1;
        cumulativeTravelTime.clear();
        cumulativeTravelTime.add(0.0);
        double elapsed = 0.0;
        for (int index = 0; index < moves; index++) {
            double distance = points[visitSequence.get(index)].distanceTo(points[visitSequence.get(index + 1)]);
            elapsed += distance / speedBound;
            cumulativeTravelTime.add(elapsed);
        }

        uniformOrderStatistics.clear();
        uniformOrderStatistics.add(0.0);
        uniformOrderStatistics.addAll(random.doubles(moves).boxed().sorted().toList());
        uniformOrderStatistics.add(1.0);
    }

    /**
     * Fills report times and places for the embedding produced by {@link #embedTrajectory}.
     *
     * @param visitSequence place indices of the trajectory
     * @param count         number of reports
     * @param delta         minimum gap of the truncated Pareto law, greater than zero
     * @param alpha         tail exponent, less than minus one
     * @param days          observation window, before subtracting travel time
     */
    public void generateReports(List<Integer> visitSequence, int count, double delta, double alpha, double days) {
        reportTimes.clear();
        reportPlaces.clear();
        int moves = visitSequence.size() - 1;
        double daysAvailable = days - cumulativeTravelTime.get(moves);
        if (daysAvailable > 0.0) {
            List<Double> timeSeries = partialSums.generate(count, delta, alpha);
            bins.assign(uniformOrderStatistics, timeSeries);
            for (int index = 0; index < count; index++) {
                int bin = bins.getPlacements().get(index);
                reportPlaces.add(visitSequence.get(bin));
                double time = cumulativeTravelTime.get(bin) + daysAvailable * timeSeries.get(index);
                reportTimes.add(Math.floor(time) + dailySeasonality.inverseCumulativeProbability(time - Math.floor(time)));
            }
        } else {
            LOGGER.warning("Duration in days is insufficient for travel. No reports generated.");
        }
    }

    public List<Double> getCumulativeTravelTime() {
        return cumulativeTravelTime;
    }

    public List<Double> getReportTimes() {
        return reportTimes;
    }

    public List<Integer> getReportPlaces() {
        return reportPlaces;
    }
}
