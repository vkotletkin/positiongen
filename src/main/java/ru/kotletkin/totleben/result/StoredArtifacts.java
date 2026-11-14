package ru.kotletkin.totleben.result;

public record StoredArtifacts(
        String xyObjectKey,
        String placesObjectKey,
        long xyByteSize,
        long placesByteSize,
        int reportCount
) {
}
