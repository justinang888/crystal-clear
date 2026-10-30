package com.spring.aurora.model;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name="PAYMENT")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="payment_id")
    private long paymentId;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="amount")
    private Double amount;

    @Column(name = "wh_tax")
    private Double withholdingTax;

    @Column(name="payment_type")
    private String paymentType;

    @Column(name="remarks")
    private String remarks;

    @Column(name="created_at")
    private Date createdAt;
}
