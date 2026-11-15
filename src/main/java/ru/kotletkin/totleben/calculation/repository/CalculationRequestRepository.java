package ru.kotletkin.totleben.calculation.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kotletkin.totleben.calculation.model.CalculationRequest;

import java.util.Optional;
import java.util.UUID;

public interface CalculationRequestRepository extends JpaRepository<CalculationRequest, UUID> {

    @Override
    @EntityGraph(attributePaths = "result")
    @NonNull
    Optional<CalculationRequest> findById(@NonNull UUID id);

    @Override
    @EntityGraph(attributePaths = "result")
    @NonNull
    Page<CalculationRequest> findAll(@NonNull Pageable pageable);
}
