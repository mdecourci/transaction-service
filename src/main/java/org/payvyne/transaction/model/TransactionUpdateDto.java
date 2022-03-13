package org.payvyne.transaction.model;

import org.payvyne.transaction.domain.TransactionStatus;

public record TransactionUpdateDto(TransactionStatus transactionStatus, String comment) {
}
