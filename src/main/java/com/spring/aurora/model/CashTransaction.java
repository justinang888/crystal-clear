package com.spring.aurora.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

//@Entity
@Data
public class CashTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private long transactionId;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name ="description")
    private String description;
    @Column(name = "other_description")
    private String otherDescription;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "createdAt")
    private Date createdAt;
}
