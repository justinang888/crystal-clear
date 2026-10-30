package com.spring.aurora.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.spring.aurora.model.Order;

public interface OrderService {
    Order insert(Order order);
    Order update(Order order);
    Order delete(Order order);

    Timestamp getMostRecentOrderDate(long customerId);
    Integer getOrderCountPerMonth(Integer month, Integer year, long customerId);

    List<Order> findAllByCustomerId(long customerId);
    List<Order> findAllOrdersToday(Date dateParam);
    List<Order> findAllByDeliveryReceiptNumber(int drNumber);
    List<Order> findAll();

    List<Order> findAllOrdersPerMonth(Integer month, Integer year);

    Order findOrderByOrderId(long orderId);

    void cancelOrder(long orderId);

    void setToDelivered(long orderId);
    String getNewDrNumber();
    
    List<Long> inactiveCustomerIds(Date dateVal);
}
