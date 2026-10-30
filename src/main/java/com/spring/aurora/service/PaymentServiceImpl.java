package com.spring.aurora.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.spring.aurora.dao.PaymentDao;
import com.spring.aurora.model.ArSummary;
import com.spring.aurora.model.ArSummaryRepository;
import com.spring.aurora.model.Payment;

import lombok.AllArgsConstructor;

@Service("paymentService")
@Transactional
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private ArSummaryRepository arSummaryRepo;
    private PaymentDao paymentDao;

    @Override
    public Payment insert(Payment payment) {
        ArSummary current = this.arSummaryRepo.findByCustomerId(payment.getCustomerId()).orElseGet(ArSummary::new);
        if (current.getCustomerId() != payment.getCustomerId()) {
            current.setCustomerId(payment.getCustomerId());
        }
        BigDecimal arAmount = current.getArAmount()
                .subtract(new BigDecimal(payment.getAmount())
                        .add(new BigDecimal(payment.getWithholdingTax())));
        current.setArAmount(arAmount);
        this.arSummaryRepo.save(current);
        return this.paymentDao.save(payment);
    }

    @Override
    public List<Payment> findAllByCustomerId(long customerId) {
        return this.paymentDao.findAllByCustomerId(customerId);
    }

    @Override
    public List<Payment> findAllByCustomerIdAndDate(long customerId, Date date) {
        return this.paymentDao.findAllByCustomerIdAndCreatedAt(customerId, date);
    }

    @Override
    public double getPaymentsTotalByCustomerId(long customerId) {
        List<Payment> payments = this.paymentDao.findAllByCustomerId(customerId);
        double total = payments.stream().mapToDouble(p -> p.getAmount() + p.getWithholdingTax()).sum();
        return total;
    }

    @Override
    public List<Payment> findAllByDate(Date date) {
        return this.paymentDao.findAllByCreatedAt(date);
    }

    @Override
    public double getTotalPayments() {
        List<Payment> payments = new ArrayList<>();
        this.paymentDao.findAll().forEach(payments::add);
        double total = payments.stream().mapToDouble(p -> p.getAmount() + p.getWithholdingTax()).sum();
        return total;
    }
}
