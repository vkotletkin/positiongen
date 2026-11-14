package ru.kotletkin.totleben.calculation.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalculationRequestRepository extends JpaRepository<CalculationRequest, UUID> {

    @Override
    @EntityGraph(attributePaths = "result")
    Optional<CalculationRequest> findById(UUID id);

    @EntityGraph(attributePaths = "result")
    List<CalculationRequest> findAllByOrderByCreatedAtDesc();
}
