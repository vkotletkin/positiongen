package ru.kotletkin.totleben.result.dto;

public record StoredArtifacts(
        String xyObjectKey,
        String placesObjectKey,
        long xyByteSize,
        long placesByteSize,
        int reportCount
) {
}
