package com.spring.aurora.dao;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Product;

public interface ProductDao extends CrudRepository<Product, Long> {
}
