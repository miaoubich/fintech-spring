package com.miaoubich.wallet.repository;

import com.miaoubich.wallet.entity.Position;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
	
    List<Position> findByUserId(String userId);
    Optional<Position> findByUserIdAndSymbol(String userId, String symbol);
}