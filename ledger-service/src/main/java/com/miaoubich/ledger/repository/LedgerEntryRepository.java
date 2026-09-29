package com.miaoubich.ledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.ledger.model.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByUserIdOrderByCreatedAtDesc(String userId);
}