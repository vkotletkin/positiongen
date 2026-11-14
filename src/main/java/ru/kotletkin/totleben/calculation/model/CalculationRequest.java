package ru.kotletkin.totleben.calculation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kotletkin.totleben.result.model.CalculationResult;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "calculation_request")
public class CalculationRequest {

    @Id
    private UUID id;

    @Column(length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CalculationStatus status;

    @Column(nullable = false)
    private int dimension;

    @Column(nullable = false)
    private double fractalDimension;

    @Column(nullable = false)
    private int pointCount;

    @Column(nullable = false)
    private int travelerCount;

    @Column(nullable = false)
    private int coTravelerCount;

    @Column(nullable = false)
    private double days;

    @Column(nullable = false)
    private double meanReportCount;

    @Column(length = 2000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private CalculationResult result;
}
