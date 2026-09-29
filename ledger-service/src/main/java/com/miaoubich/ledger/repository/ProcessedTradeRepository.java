package com.miaoubich.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.ledger.model.ProcessedTrade;

public interface ProcessedTradeRepository extends JpaRepository<ProcessedTrade, String> {

    boolean existsByTradeId(String tradeId);
}