package ru.kotletkin.positiongen.simulation;

public record SimulationRequest(
        int dimension,
        double fractalDimension,
        int pointCount,
        int travelerCount,
        int coTravelerCount,
        int days,
        int meanReportCount,
        double restartRate,
        double distanceExponent,
        double maxKilometersPerHour,
        double kilometersPerUnit,
        double phiMean,
        double stepsMean,
        double reportDelta,
        double reportAlpha,
        double[][] places
) {

    public SimulationRequest withPlaces(final double[][] places) {
        return new SimulationRequest(
                dimension,
                fractalDimension,
                places == null ? pointCount : places.length,
                travelerCount,
                coTravelerCount,
                days,
                meanReportCount,
                restartRate,
                distanceExponent,
                maxKilometersPerHour,
                kilometersPerUnit,
                phiMean,
                stepsMean,
                reportDelta,
                reportAlpha,
                places);
    }
}
