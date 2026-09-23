package com.miaoubich.ledger.repository;

import com.miaoubich.ledger.model.LedgerEntry;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByUserIdOrderByCreatedAtDesc(String userId);
}