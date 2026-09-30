package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.OpenTableAbandonmentModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository for abandoned open table creation flows
@Repository
public interface OpenTableAbandonmentRepository extends JpaRepository<OpenTableAbandonmentModel, Long> {
}
