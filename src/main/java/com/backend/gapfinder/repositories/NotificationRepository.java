package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.NotificationModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
}
