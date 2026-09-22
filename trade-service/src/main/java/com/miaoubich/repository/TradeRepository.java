package com.miaoubich.repository;

import java.util.List;
import java.util.Optional;

import com.miaoubich.model.Trade;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

	@Query("SELECT t FROM Trade t ORDER BY t.createdAt DESC")
    List<Trade> findAllTradesOrderByCreatedAtDesc();

    List<Trade> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Trade> findByTradeId(String tradeId);
}