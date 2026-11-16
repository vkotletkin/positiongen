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
        double reportAlpha
) {
}
