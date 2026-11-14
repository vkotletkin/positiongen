package ru.kotletkin.totleben.simulation;

public record SimulationRequest(
        int dimension,
        double fractalDimension,
        int pointCount,
        int travelerCount,
        int coTravelerCount,
        double days,
        double meanReportCount
) {
}
