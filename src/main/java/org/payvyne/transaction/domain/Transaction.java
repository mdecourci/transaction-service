package org.payvyne.transaction.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "TRANSACTION")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Long transactionId;

    @Column(nullable = false, updatable = false)
    @NotNull(message = "transactionDate is mandatory")
    private LocalDateTime transactionDate;

    @NotNull(message = "transactionStatus is mandatory")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @NotNull(message = "currencyCode is mandatory")
    @Column(nullable = false)
    private String currencyCode;

    @NotNull(message = "an amount is mandatory")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    @NotNull
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime modifiedDate;

    private String comment;
}
