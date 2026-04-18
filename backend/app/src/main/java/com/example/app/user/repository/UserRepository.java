package com.example.app.user.repository;

import com.example.app.user.entity.User;
import com.example.app.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	List<User> findAllByRoleAndActiveTrueOrderByFullNameAsc(UserRole role);
}
