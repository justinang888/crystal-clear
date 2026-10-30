package com.spring.aurora.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
