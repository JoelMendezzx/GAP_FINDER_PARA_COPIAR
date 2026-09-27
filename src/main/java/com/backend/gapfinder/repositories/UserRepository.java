package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {
}