package com.backend.gapfinder.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.gapfinder.models.RefreshTokenModel;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {
}
