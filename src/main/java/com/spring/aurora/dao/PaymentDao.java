package com.spring.aurora.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Payment;

public interface PaymentDao extends CrudRepository<Payment, Long> {
    List<Payment> findAllByCustomerId(long customerId);

    List<Payment> findAllByCustomerIdAndCreatedAt(long customerId, Date createdAt);

    List<Payment> findAllByCreatedAt(Date createdAt);

    /** Payments on any day from {@code from} to {@code to}, both included, whatever their time of day. */
    @Query("select p from Payment p where cast(p.createdAt as date) between :from and :to")
    List<Payment> findAllBetween(java.sql.Date from, java.sql.Date to);
}
