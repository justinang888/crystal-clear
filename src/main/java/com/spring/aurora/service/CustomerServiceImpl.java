package com.spring.aurora.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.ContainerDao;
import com.spring.aurora.dao.CustomerDao;
import com.spring.aurora.dao.DebtDao;
import com.spring.aurora.dao.OrderDao;
import com.spring.aurora.dao.PaymentDao;
import com.spring.aurora.entity.CustomerWithOrder;
import com.spring.aurora.entity.CustomerWithPrice;
import com.spring.aurora.model.Customer;

@Service("customerService")
@Transactional
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerDao customerDao;
    private OrderDao orderDao;
    private ContainerDao containerDao;
    private DebtDao debtDao;
    private PaymentDao paymentDao;

    public CustomerDao getCustomerDao() {
        return this.customerDao;
    }

    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public OrderDao getOrderDao() {
        return this.orderDao;
    }

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public ContainerDao getContainerDao() {
        return this.containerDao;
    }

    public void setContainerDao(ContainerDao containerDao) {
        this.containerDao = containerDao;
    }

    public DebtDao getDebtDao() {
        return this.debtDao;
    }

    public void setDebtDao(DebtDao debtDao) {
        this.debtDao = debtDao;
    }

    public PaymentDao getPaymentDao() {
        return this.paymentDao;
    }

    public void setPaymentDao(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Override
    public Customer insert(Customer customer) {
        return this.customerDao.save(customer);
    }

    @Override
    public Customer update(Customer customer) {
        return this.customerDao.save(customer);
    }

    @Override
    public Customer find(Customer customer) {
        return this.customerDao.findById(customer.getCustomerId()).orElseGet(Customer::new);
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> result = new ArrayList<>();
        this.customerDao.findAll().forEach(result::add);
        return result;
    }

    @Override
    public Customer view(long customerId) {
        return this.customerDao.findById(customerId).orElseGet(Customer::new);
    }

    @Override
    public List<Customer> findAllByCustomerIdIn(List<Long> customerIdList) {
        return this.customerDao.findAllByCustomerIdIn(customerIdList);
    }

    @Override
    public List<CustomerWithPrice> findAllCustomersWithPrice() {
        return this.customerDao.findAllCustomersWithPrice();
    }

    @Override
    public Customer findByCustomerId(long customerId) {
        return this.customerDao.findById(customerId).orElseGet(Customer::new);
    }

    @Override
    public List<CustomerWithOrder> findMonthlyActiveCustomers(int month, int year) {
        return this.customerDao.findMonthlyActiveCustomers(month, year);
    }
}
