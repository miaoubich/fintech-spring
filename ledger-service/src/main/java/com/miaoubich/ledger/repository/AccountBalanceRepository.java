package com.miaoubich.ledger.repository;

import com.miaoubich.ledger.model.AccountBalance;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    Optional<AccountBalance> findByUserIdAndSymbol(String userId, String symbol);
}