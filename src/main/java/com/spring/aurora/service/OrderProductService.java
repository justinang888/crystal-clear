package com.spring.aurora.service;

import java.util.List;

import com.spring.aurora.model.OrderProduct;

public interface OrderProductService {
    OrderProduct insert(OrderProduct orderProduct);
    OrderProduct update(OrderProduct orderProduct);
    void remove(OrderProduct orderProduct);

    List<OrderProduct> findAllByOrderId(long orderId);
}
