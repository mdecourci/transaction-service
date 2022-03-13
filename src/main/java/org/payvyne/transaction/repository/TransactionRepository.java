package org.payvyne.transaction.repository;

import org.payvyne.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByTransactionDateBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageRequest);
}
