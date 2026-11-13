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
    /** Orders on any day from {@code from} to {@code to}, both included. */
    List<Order> findAllOrdersBetween(java.sql.Date from, java.sql.Date to);
    /** Delivered orders from {@code from} up to, but not including, {@code until}. */
    List<Order> findDeliveredBetween(Timestamp from, Timestamp until);
    List<Order> findAllByDeliveryReceiptNumber(int drNumber);
    List<Order> findAll();
    /** Every pending order, from any day, oldest first. */
    List<Order> findAllPending();

    List<Order> findAllOrdersPerMonth(Integer month, Integer year);

    Order findOrderByOrderId(long orderId);

    void cancelOrder(long orderId);

    void setToDelivered(long orderId);
    String getNewDrNumber();
    
    List<Long> inactiveCustomerIds(Date dateVal);
}
