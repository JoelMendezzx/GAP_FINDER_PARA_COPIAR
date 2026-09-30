package com.backend.gapfinder.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.gapfinder.models.GoogleCredentialModel;

public interface GoogleCredentialRepository extends JpaRepository<GoogleCredentialModel, Long> {
}
