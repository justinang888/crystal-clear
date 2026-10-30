package com.spring.aurora.model;

import java.sql.Date;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name="DEBT")
@Data
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="debt_id")
    private long debtId;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="amount")
    private Double amount = 0.0;

    @Column(name="remarks")
    private String remarks = "";

    @Column(name="created_at")
    private Date createdAt = new Date(System.currentTimeMillis());

    @Column(name="linked_order_id")
    private Long orderId;
}
