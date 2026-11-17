package ru.kotletkin.positiongen.calculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "calculation_places")
public class CalculationPlaces {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String modelCoordinates;
}
