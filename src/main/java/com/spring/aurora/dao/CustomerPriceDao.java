package com.spring.aurora.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.CustomerPrice;

public interface CustomerPriceDao extends CrudRepository<CustomerPrice, Long> {

    List<CustomerPrice> findAllByCustomerId(long customerId);

    List<CustomerPrice> findAllByProductId(long productId);

    @Modifying
    @Query("delete from CustomerPrice cp where cp.customerId = :customerId and cp.productId = :productId")
    void deleteCustomerPrice(long customerId, long productId);
}
