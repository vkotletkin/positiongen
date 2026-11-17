package ru.kotletkin.positiongen.calculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kotletkin.positiongen.calculation.model.CalculationPlaces;

import java.util.UUID;

public interface CalculationPlacesRepository extends JpaRepository<CalculationPlaces, UUID> {
}
