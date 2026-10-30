package com.spring.aurora.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
