package com.miaoubich.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.auth.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
	
	boolean existByUsername(String username);
	
	boolean existByEmail(String email);
	
	// User by the 1-minute @Schedular job to find all unsynced users
	List<User> findBySyncedFalse();
}
