package com.miaoubich.ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.ledger.model.AccountBalance;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    Optional<AccountBalance> findByUserIdAndSymbol(String userId, String symbol);
}