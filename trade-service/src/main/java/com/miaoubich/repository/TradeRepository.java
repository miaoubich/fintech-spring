package com.miaoubich.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miaoubich.model.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {

	@Query("SELECT t FROM Trade t ORDER BY t.createdAt DESC")
    List<Trade> findAllTradesOrderByCreatedAtDesc();

    List<Trade> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Trade> findByTradeId(String tradeId);
}