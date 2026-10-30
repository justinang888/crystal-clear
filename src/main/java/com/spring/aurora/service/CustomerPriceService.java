package com.spring.aurora.service;

import java.util.List;

import com.spring.aurora.model.CustomerPrice;

public interface CustomerPriceService {
    CustomerPrice saveOrUpdate(CustomerPrice customerPrice);

    List<CustomerPrice> findAllByCustomerId(long customerId);

    List<CustomerPrice> findAllByProductId(long productId);

    void deleteCustomerPrice(long customerId, long productId);
}
