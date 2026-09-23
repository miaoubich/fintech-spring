package com.miaoubich.ledger.repository;

import com.miaoubich.ledger.model.ProcessedTrade;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface ProcessedTradeRepository extends JpaRepository<ProcessedTrade, String> {

    boolean existsByTradeId(String tradeId);
}