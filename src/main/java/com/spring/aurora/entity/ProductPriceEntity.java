package com.spring.aurora.entity;

import com.spring.aurora.model.Customer;
import com.spring.aurora.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductPriceEntity {
    private long priceId;
    private Customer customer;
    private Product product;
    private Double sellingPrice;
}
