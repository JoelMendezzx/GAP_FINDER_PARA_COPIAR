package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.FriendshipModel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<FriendshipModel, Long> {
}
