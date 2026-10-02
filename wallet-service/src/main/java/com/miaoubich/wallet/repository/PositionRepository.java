package com.miaoubich.wallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.wallet.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
	
    List<Position> findByUserId(String userId);
    Optional<Position> findByUserIdAndSymbol(String userId, String symbol);
}