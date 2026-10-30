package com.spring.aurora.service;

import java.util.List;

import com.spring.aurora.model.Product;

public interface ProductService {
    Product save(Product product);
    List<Product> findAll();

    Product findByProductId(long productId);
}
