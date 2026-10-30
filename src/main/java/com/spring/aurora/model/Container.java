package com.spring.aurora.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="CONTAINER")
@Data
@Getter
@Setter
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="container_id")
    private long containerId;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="order_id")
    private Long orderId = 0L;

    @Column(name="status")
    private String status = "";

    @Column(name="round_count")
    private int roundCount;

    @Column(name="slim_count")
    private int slimCount;

    @Column(name="created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}
