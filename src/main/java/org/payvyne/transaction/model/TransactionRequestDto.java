package org.payvyne.transaction.model;

import java.math.BigDecimal;

public record TransactionRequestDto(BigDecimal amount, String currencyCode, String transactionDate, String comment) {
}
