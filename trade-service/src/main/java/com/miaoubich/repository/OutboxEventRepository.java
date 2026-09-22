package com.miaoubich.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miaoubich.model.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = "SELECT * FROM outbox_events WHERE processed = false ORDER BY created_at ASC FOR UPDATE SKIP LOCKED", 
    		nativeQuery = true)
    List<OutboxEvent> findUnprocessedEventsForUpdate();
    long countByProcessed(boolean processed);
}

