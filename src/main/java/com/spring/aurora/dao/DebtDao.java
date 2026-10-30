package com.spring.aurora.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Debt;

public interface DebtDao extends CrudRepository<Debt, Long> {
    List<Debt> findAllByCustomerId(long customerId);
    List<Debt> findByCustomerIdAndOrderId(long customerId, long orderId);
}
