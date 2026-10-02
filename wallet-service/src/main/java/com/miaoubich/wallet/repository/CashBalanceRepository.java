package com.miaoubich.wallet.repository;

import java.util.List;
import java.util.Optional;

import com.miaoubich.wallet.entity.CashBalance;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface CashBalanceRepository extends JpaRepository<CashBalance, Long> {
	
    List<CashBalance> findByUserId(String userId);
    Optional<CashBalance> findByUserIdAndCurrency(String userId, String currency);
}