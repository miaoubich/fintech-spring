package com.miaoubich.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miaoubich.model.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
		    		SELECT * FROM outbox_events 
		    			WHERE e.status = 'PENDING' 
		    			ORDER BY e.created_at ASC 
		    			LIMIT 100 
		    			FOR UPDATE SKIP LOCKED
		    		""", 
    		nativeQuery = true)
    List<OutboxEvent> findPendingEventsForUpdate();
    long countByProcessed(boolean processed);
    
    /**
     * Find all dead letter events for monitoring or manual re-processing.
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtDesc(OutboxEvent.Status status);
    
}

