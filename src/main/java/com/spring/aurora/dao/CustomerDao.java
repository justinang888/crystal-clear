package com.spring.aurora.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.entity.CustomerWithOrder;
import com.spring.aurora.entity.CustomerWithPrice;
import com.spring.aurora.model.Customer;

public interface CustomerDao extends CrudRepository<Customer, Long> {
    List<Customer> findAllByCustomerIdIn(List<Long> customerIds);
    
    //@Query(nativeQuery=true, value="select cp.selling_price as sellingPrice, cp.product_id as productId, c.* from aurora.customer c, aurora.customer_price cp where c.customer_id = cp.customer_id;")
    @Query("select new com.spring.aurora.entity.CustomerWithPrice(c.customerId, c.type, c.name, c.address, c.contactName, c.mainNumber, c.alternateNumber, c.emailAddress, c.orderInterval, "
            + "c.totalRound, c.totalSlim, cp.productId, cp.sellingPrice) from Customer as c, CustomerPrice as cp where c.customerId = cp.customerId")
    List<CustomerWithPrice> findAllCustomersWithPrice();
    
    @Query("select new com.spring.aurora.entity.CustomerWithOrder(c.customerId, c.type, c.name, c.address, c.contactName, c.mainNumber, c.alternateNumber, o.orderId) "
            + "from Customer as c, Order as o where c.customerId = o.customerId and MONTH(o.createdAt) = :month and YEAR(o.createdAt) = :year group by c.customerId, o.orderId")
    List<CustomerWithOrder> findMonthlyActiveCustomers(int month, int year);
}
