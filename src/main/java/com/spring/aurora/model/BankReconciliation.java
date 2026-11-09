package com.spring.aurora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The owner's monthly bank reconciliation: the bank transfers out of the account that month as
 * the statement shows them, and a note. Compared on screen with the Bank Transfer expenses.
 */
@Entity
@Table(name = "bank_reconciliation")
@Data
@NoArgsConstructor
public class BankReconciliation {

    /** The first day of the month. */
    @Id
    @Column(name = "month")
    private LocalDate month;

    @Column(name = "statement_amount")
    private BigDecimal statementAmount;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
