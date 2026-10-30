package com.spring.aurora.model;

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
@Table(name="ORDER_PRODUCT")
@Data
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_product_id")
    private long orderProductId;

    @Column(name="order_id")
    private long orderId;

    @Column(name="product_id")
    private long productId;

    @Column(name="quantity")
    @Convert(converter = StringToIntegerConverter.class)
    private String quantity;
}
