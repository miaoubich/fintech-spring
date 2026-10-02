package com.miaoubich.wallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.wallet.entity.CashBalance;

public interface CashBalanceRepository extends JpaRepository<CashBalance, Long> {
	
    List<CashBalance> findByUserId(String userId);
    Optional<CashBalance> findByUserIdAndCurrency(String userId, String currency);
}