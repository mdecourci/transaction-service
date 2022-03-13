package org.payvyne.transaction.api;

import lombok.RequiredArgsConstructor;
import org.payvyne.transaction.domain.Transaction;
import org.payvyne.transaction.domain.TransactionStatus;
import org.payvyne.transaction.exception.TransactionNotFoundException;
import org.payvyne.transaction.model.TransactionRequestDto;
import org.payvyne.transaction.model.TransactionUpdateDto;
import org.payvyne.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/transaction")
    public Page<Transaction> findAllTransactions(@PageableDefault(size = 25, value = 0, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.find(pageable);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transaction")
    public Long createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        Currency.getInstance(transactionRequestDto.currencyCode());

        return transactionService.save(Transaction.builder()
                        .transactionStatus(TransactionStatus.CREATED)
                        .transactionDate(LocalDateTime.parse(transactionRequestDto.transactionDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                        .amount(transactionRequestDto.amount())
                        .currencyCode(transactionRequestDto.currencyCode())
                        .comment(transactionRequestDto.comment())
                        .build())
                .getTransactionId();
    }

    @GetMapping("/transaction/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionService.find(id).orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    @GetMapping("/transaction/search")
    public Page<Transaction> getTransactionBetweenDates(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate, @PageableDefault(size = 25, value = 0, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        if (fromDate == null || toDate == null || (toDate.isBefore(fromDate))) {
            String.format("Invalid from date %s and to date %s", fromDate.format(DateTimeFormatter.ISO_DATE), toDate.format(DateTimeFormatter.ISO_DATE));
            throw new IllegalArgumentException("Invalid from date");
        }
        return transactionService.find(fromDate, toDate, pageable);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/transaction/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/transaction")
    public void deleteTransactions() {
        transactionService.deleteAll();
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/transaction/{id}")
    public void updateTransaction(@PathVariable Long id, @RequestBody TransactionUpdateDto transactionUpdateDto) {
        transactionService.update(id, transactionUpdateDto);
    }
}