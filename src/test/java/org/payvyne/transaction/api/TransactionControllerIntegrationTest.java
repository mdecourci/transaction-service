package org.payvyne.transaction.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.payvyne.transaction.Application;
import org.payvyne.transaction.domain.Transaction;
import org.payvyne.transaction.domain.TransactionStatus;
import org.payvyne.transaction.model.TransactionRequestDto;
import org.payvyne.transaction.model.TransactionUpdateDto;
import org.payvyne.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionControllerIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    LocalDateTime testTransactionDate = LocalDateTime.of(2020, Month.JANUARY, 1, 13, 30, 30);
    @Autowired
    private TransactionRepository transactionRepository;

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
    }

    @Test
    public void testFindingAllTransactions() throws Exception {
        final var transactions = List.of(Transaction.builder().transactionDate(LocalDateTime.now().minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.CREATED).currencyCode("GBP").amount(BigDecimal.valueOf(300.00).setScale(2, RoundingMode.HALF_UP)).comment("client1").build(), Transaction.builder().transactionDate(LocalDateTime.now().minusYears(1).minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.ACTIVE).currencyCode("GBP").amount(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_UP)).comment("client2").build());

        transactionRepository.saveAll(transactions);

        final var result = testRestTemplate
                .withBasicAuth("john123", "password")
                .exchange(URI.create("/api/v1/transaction/"), HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestPageResponse<Transaction>>() {
                        }
                );

        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody().getTotalElements(), equalTo(2L));

        assertThat(result.getBody().getContent().get(0), equalTo(transactions.get(0)));
        assertThat(result.getBody().getContent().get(1), equalTo(transactions.get(1)));
    }

    @Test
    public void testFindingOneTransaction() throws Exception {

        final var transaction = Transaction.builder().transactionDate(testTransactionDate).transactionStatus(TransactionStatus.APPROVED).currencyCode("GBP").amount(BigDecimal.valueOf(900).setScale(2, RoundingMode.HALF_UP)).comment("client").build();

        transactionRepository.save(transaction);

        final var result = testRestTemplate.withBasicAuth("john123", "password").getForEntity("/api/v1/transaction/{id}", Transaction.class, Map.of("id", transaction.getTransactionId()));

        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));

        assertThat(result.getBody(), equalTo(transaction));
    }

    @Test
    void testCreatingTransaction() {

        final var transactionRequestDto = new TransactionRequestDto(BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP), "GBP", testTransactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), "Create a new transaction");

        HttpHeaders headers = new HttpHeaders();
        final var httpEntity = new HttpEntity<>(transactionRequestDto, headers);

        final var result = testRestTemplate.withBasicAuth("john123", "password").postForEntity("/api/v1/transaction/", httpEntity, Long.class);

        assertThat(result.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(result.getBody(), greaterThan(0L));
        assertThat(transactionRepository.findById(result.getBody()).isPresent(), equalTo(true));

        assertThat(transactionRepository.findById(result.getBody()).get().getTransactionStatus(), equalTo(TransactionStatus.CREATED));
        assertThat(transactionRepository.findById(result.getBody()).get().getComment(), equalTo(transactionRequestDto.comment()));
        assertThat(transactionRepository.findById(result.getBody()).get().getAmount(), equalTo(transactionRequestDto.amount()));
    }

    @Test
    void testCreatingTransactionNoAuthentication() {

        final var transactionRequestDto = new TransactionRequestDto(BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP), "GBP", testTransactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), "Create a new transaction");

        HttpHeaders headers = new HttpHeaders();
        final var httpEntity = new HttpEntity<>(transactionRequestDto, headers);

        final var result = testRestTemplate.postForEntity("/api/v1/transaction/", httpEntity, Long.class);

        assertThat(result.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void testFindingTransactionsBetweenDates() {
        LocalDateTime testLocalDateTime = LocalDateTime.of(2022, Month.JANUARY, 1, 15, 30, 30);

        final var transactions = List.of(
                Transaction.builder()
                        .transactionDate(testLocalDateTime.minusYears(15))
                        .transactionStatus(TransactionStatus.CREATED)
                        .currencyCode("GBP").amount(BigDecimal.valueOf(300.00).setScale(2, RoundingMode.HALF_UP))
                        .comment("15 years in past")
                        .build(),
                Transaction.builder()
                        .transactionDate(testLocalDateTime.minusYears(10))
                        .transactionStatus(TransactionStatus.ACTIVE)
                        .currencyCode("GBP")
                        .amount(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_UP))
                        .comment("10 years in past")
                        .build(),
                Transaction.builder()
                        .transactionDate(testLocalDateTime.minusYears(5))
                        .transactionStatus(TransactionStatus.APPROVED)
                        .currencyCode("GBP")
                        .amount(BigDecimal.valueOf(2000.00).setScale(2, RoundingMode.HALF_UP))
                        .comment("5 years in past")
                        .build());

        transactionRepository.saveAll(transactions);

        final var result = testRestTemplate
                .withBasicAuth("john123", "password")
                .exchange("/api/v1/transaction/search/?fromDate={fromDate}&toDate={toDate}", HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestPageResponse<Transaction>>() {
                        },
                        Map.of("fromDate", "2010-12-01", "toDate", "2020-12-01")
                );

        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody().getTotalElements(), equalTo(2L));

        final var expectedTransaction10Year = transactions.get(1);
        final var expectedTransactionId10Year = expectedTransaction10Year.getTransactionId();
        final var actualTransaction10Years = result.getBody().getContent().stream().filter(content -> content.getTransactionId() == expectedTransactionId10Year).findFirst().get();

        assertThat(actualTransaction10Years, equalTo(expectedTransaction10Year));

        final var expectedTransaction5Year = transactions.get(2);
        final var expectedTransactionId5Year = expectedTransaction5Year.getTransactionId();
        final var actualTransaction5Years = result.getBody().getContent().stream().filter(content -> content.getTransactionId() == expectedTransactionId5Year).findFirst().get();

        assertThat(actualTransaction5Years, equalTo(expectedTransaction5Year));
    }

    @Test
    void testDeleteTransaction() {
        final var transactions = List.of(Transaction.builder().transactionDate(LocalDateTime.now().minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.CREATED).currencyCode("GBP").amount(BigDecimal.valueOf(300.00).setScale(2, RoundingMode.HALF_UP)).comment("client1").build(), Transaction.builder().transactionDate(LocalDateTime.now().minusYears(1).minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.ACTIVE).currencyCode("GBP").amount(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_UP)).comment("client2").build());

        transactionRepository.saveAll(transactions);
        final var transactionList = transactionRepository.findAll();
        assertThat(transactionList, hasSize(2));

        testRestTemplate.withBasicAuth("john123", "password").
                delete("/api/v1/transaction/{id}", Map.of("id", transactionList.get(0).getTransactionId()));

        final var remainingTransactionList = transactionRepository.findAll();
        assertThat(remainingTransactionList, hasSize(1));
        assertThat(remainingTransactionList.get(0), equalTo(transactions.get(1)));
    }

    @Test
    void testDeleteAllTransactions() {
        final var transactions = List.of(Transaction.builder().transactionDate(LocalDateTime.now().minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.CREATED).currencyCode("GBP").amount(BigDecimal.valueOf(300.00).setScale(2, RoundingMode.HALF_UP)).comment("client1").build(), Transaction.builder().transactionDate(LocalDateTime.now().minusYears(1).minusMonths(3).minusDays(2)).transactionStatus(TransactionStatus.ACTIVE).currencyCode("GBP").amount(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_UP)).comment("client2").build());

        transactionRepository.saveAll(transactions);

        testRestTemplate.withBasicAuth("john123", "password").delete(URI.create("/api/v1/transaction/"));

        assertThat(transactionRepository.findAll(), empty());
    }

    @Test
    void testUpdateTransaction() {

        final var transaction = Transaction.builder()
                .transactionDate(testTransactionDate)
                .transactionStatus(TransactionStatus.ACTIVE)
                .currencyCode("GBP")
                .amount(BigDecimal.valueOf(900).setScale(2, RoundingMode.HALF_UP))
                .comment("client")
                .build();

        final var savedTransaction = transactionRepository.save(transaction);

        final var transactionUpdate = new TransactionUpdateDto(TransactionStatus.APPROVED, "Approved transaction");
        HttpHeaders headers = new HttpHeaders();
        final var httpEntity = new HttpEntity<>(transactionUpdate, headers);

        testRestTemplate.withBasicAuth("john123", "password")
                .put("/api/v1/transaction/{id}", httpEntity, Map.of("id", transaction.getTransactionId()));

        final var updatedOptionalTransaction = transactionRepository.findById(savedTransaction.getTransactionId());

        assertThat(updatedOptionalTransaction.isPresent(), equalTo(true));
        assertThat(updatedOptionalTransaction.get().getTransactionStatus(), equalTo(TransactionStatus.APPROVED));
        assertThat(updatedOptionalTransaction.get().getComment(), equalTo("Approved transaction"));
    }
}