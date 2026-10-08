package com.miaoubich.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.model.OutboxDeadLetter;

public interface OutboxDeadLetterRepository extends JpaRepository<OutboxDeadLetter, Long> {

}
