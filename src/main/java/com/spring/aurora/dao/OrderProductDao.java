package com.spring.aurora.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.OrderProduct;

public interface OrderProductDao extends CrudRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrderId(long orderId);
}
