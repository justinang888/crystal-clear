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
@Table(name="EXPENSE")
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="expense_id")
    private long expenseId;

    @Column(name="description")
    private String description;

    @Column(name="amount")
    private Double amount;

    @Column(name="created_at")
    //@Convert(converter = com.spring.aurora.util.LocalDateAttributeConverter.class)
    private Date createdAt;
}
