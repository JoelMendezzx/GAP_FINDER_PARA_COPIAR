package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.OpenTableModel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenTableRepository extends JpaRepository<OpenTableModel, Long> {
}
