package ru.kotletkin.positiongen.result;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kotletkin.positiongen.calculation.model.CalculationRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @ElementCollection
    @CollectionTable(name = "calculation_co_traveler", joinColumns = @JoinColumn(name = "result_id"))
    @Column(name = "traveler_id", nullable = false)
    @OrderColumn(name = "list_index")
    private List<Integer> coTravelerIds = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;
}
