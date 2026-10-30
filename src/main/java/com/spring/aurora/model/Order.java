package com.spring.aurora.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.spring.aurora.util.StringToIntegerConverter;

import lombok.Data;

@Entity
@Table(name="ORDERS")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private long orderId;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="delivery_receipt_num")
    private String deliveryReceiptNum = "";

    @Column(name="status")
    private String status = "";

    @Column(name="amount_paid")
    private Double amountPaid = 0.0;

    @Column(name="total_amount")
    private Double totalAmount = 0.0;

    @Column(name="slim_refill_only_count")
    private int slimRefillOnlyCount;

    @Column(name="round_refill_only_count")
    private int roundRefillOnlyCount;

    @Column(name="slim_container_only_count")
    private int slimContainerOnlyCount;

    @Column(name="round_container_only_count")
    private int roundContainerOnlyCount;

    @Column(name="slim_refill_with_container_count")
    private int slimRefillWithContainerCount;

    @Column(name="round_refill_with_container_count")
    private int roundRefillWithContainerCount;

    @Column(name="round_free_count")
    private int roundFreeCount;

    @Column(name="slim_free_count")
    private int slimFreeCount;

    @Column(name="cont_slim_returned")
    @Convert(converter = StringToIntegerConverter.class)
    private String slimReturned = "0";

    @Column(name="cont_round_returned")
    @Convert(converter = StringToIntegerConverter.class)
    private String roundReturned = "0";

    @Column(name="created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name="remarks")
    private String remarks = "";

    public boolean isNew() {
        return (this.orderId == 0);
    }
}