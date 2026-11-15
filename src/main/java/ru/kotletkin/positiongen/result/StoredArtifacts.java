package ru.kotletkin.positiongen.result;

public record StoredArtifacts(
        String xyObjectKey,
        String placesObjectKey,
        long xyByteSize,
        long placesByteSize,
        int reportCount
) {
}
