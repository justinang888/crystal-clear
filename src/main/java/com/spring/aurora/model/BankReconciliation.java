package com.spring.aurora.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The owner's bank reconciliation for a day or a month: the bank transfers out of the account in
 * that period as the statement shows them, and a note. Compared on screen with the Bank Transfer
 * expenses. A day's entry and its month's entry are separate.
 */
@Entity
@Table(name = "bank_reconciliation")
@IdClass(BankReconciliation.Key.class)
@Data
@NoArgsConstructor
public class BankReconciliation {

    public static final String DAY = "DAY";
    public static final String MONTH = "MONTH";

    /** {@link #DAY} or {@link #MONTH}. */
    @Id
    @Column(name = "period")
    private String period;

    /** The day, or the first day of the month. */
    @Id
    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "statement_amount")
    private BigDecimal statementAmount;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String period;
        private LocalDate periodStart;
    }
}
