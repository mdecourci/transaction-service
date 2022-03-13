package org.payvyne.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.payvyne.transaction.domain.Transaction;
import org.payvyne.transaction.domain.TransactionStatus;
import org.payvyne.transaction.model.TransactionUpdateDto;
import org.payvyne.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<Transaction> find(Pageable pageable) {
        log.info(">> Find all transactions");
        return transactionRepository.findAll(pageable);
    }

    public Transaction save(Transaction transaction) {
        log.info(">> Create/Save a new transaction");
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> find(Long id) {
        log.info(">> Find the transaction with id = [()]", id);
        return transactionRepository.findById(id);
    }

    public Page<Transaction> find(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.info(">> find: from date = [{}], to date = [{}], with a page = [{}]", fromDate, toDate, pageable);
        return transactionRepository.findByTransactionDateBetween(LocalDateTime.of(fromDate, LocalTime.of(0, 0, 0)), LocalDateTime.of(toDate, LocalTime.of(0, 0, 0)), pageable);
    }

    public void delete(Long id) {
        log.info(">> delete: transaction with id = [{}]", id);
        transactionRepository.findById(id).ifPresent(transactionRepository::delete);
    }

    public void update(Long id, TransactionUpdateDto transactionUpdateDto) {
        log.info(">> update: transactionDto with id = [{}] and update [{}] ", id, transactionUpdateDto);

        if (transactionUpdateDto != null && transactionUpdateDto.transactionStatus() == TransactionStatus.CREATED) {
            throw new IllegalArgumentException("Already created transactions cannot be recreated");
        }

        transactionRepository.findById(id)
                .ifPresent(transaction -> {
                    var transactionBuilder = transaction.toBuilder();

                    if (transactionUpdateDto.comment() != null) {
                        transactionBuilder.comment(transactionUpdateDto.comment());
                    }

                    if (transactionUpdateDto.transactionStatus() != null) {
                        transactionBuilder.transactionStatus(transactionUpdateDto.transactionStatus());
                    }

                    transactionRepository.save(transactionBuilder.build());
                });
    }

    public void deleteAll() {
        log.info(">> delete all transactions");
        transactionRepository.deleteAll();
    }
}
