package com.spring.aurora.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.CustomerPriceDao;
import com.spring.aurora.model.CustomerPrice;

@Service("customerPriceService")
@Transactional
public class CustomerPriceServiceImpl implements CustomerPriceService {

    @Autowired
    private CustomerPriceDao customerPriceDao;

    @Override
    public CustomerPrice saveOrUpdate(CustomerPrice customerPrice) {
        return this.customerPriceDao.save(customerPrice);
    }

    @Override
    public List<CustomerPrice> findAllByCustomerId(long customerId) {
        return this.customerPriceDao.findAllByCustomerId(customerId);
    }

    @Override
    public List<CustomerPrice> findAllByProductId(long productId) {
        return this.customerPriceDao.findAllByProductId(productId);
    }

    @Override
    public void deleteCustomerPrice(long customerId, long productId) {
        this.customerPriceDao.deleteCustomerPrice(customerId, productId);
    }
}
