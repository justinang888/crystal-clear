package com.spring.aurora.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Payment;

public interface PaymentDao extends CrudRepository<Payment, Long> {
    List<Payment> findAllByCustomerId(long customerId);

    List<Payment> findAllByCustomerIdAndCreatedAt(long customerId, Date createdAt);

    List<Payment> findAllByCreatedAt(Date createdAt);
}
