package com.spring.aurora.service;

import java.sql.Date;
import java.util.List;

import com.spring.aurora.model.Payment;

public interface PaymentService {
    Payment insert(Payment payment);

    List<Payment> findAllByCustomerId(long customerId);

    List<Payment> findAllByCustomerIdAndDate(long customerId, Date date);
    List<Payment> findAllByDate(Date date);

    double getPaymentsTotalByCustomerId(long customerId);
    double getTotalPayments();
}
