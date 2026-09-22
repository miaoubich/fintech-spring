package com.miaoubich.repository;

import java.util.List;

import com.miaoubich.model.OutboxEvent;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = "SELECT * FROM outbox_events WHERE processed = false ORDER BY created_at ASC FOR UPDATE SKIP LOCKED", 
    		nativeQuery = true)
    List<OutboxEvent> findUnprocessedEventsForUpdate();
    long countByProcessed(boolean processed);
}

