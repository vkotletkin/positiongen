package ru.kotletkin.totleben.result;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "calculation_result")
public class CalculationResult {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private CalculationRequest request;

    @Column(nullable = false, length = 512)
    private String xyObjectKey;

    @Column(nullable = false, length = 512)
    private String placesObjectKey;

    @Column(nullable = false)
    private long xyByteSize;

    @Column(nullable = false)
    private long placesByteSize;

    @Column(nullable = false)
    private int reportCount;

    @Column(nullable = false)
    private Instant createdAt;
}
