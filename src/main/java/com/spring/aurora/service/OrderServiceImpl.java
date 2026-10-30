package com.spring.aurora.service;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.OrderDao;
import com.spring.aurora.model.Order;

@Service("orderService")
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public Order insert(Order order) {
        return this.orderDao.save(order);
    }

    @Override
    public Order update(Order order) {
        return this.orderDao.save(order);
    }

    @Override
    public Order delete(Order order) {
        this.orderDao.delete(order);
        return order;
    }

    @Override
    public List<Order> findAllByCustomerId(long customerId) {
        return this.orderDao.findAllByCustomerId(customerId);
    }

    @Override
    public List<Order> findAllByDeliveryReceiptNumber(int drNumber) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> result = new ArrayList<>();
        this.orderDao.findAll().forEach(result::add);
        return result;
    }

    @Override
    public Timestamp getMostRecentOrderDate(long customerId) {
        return this.orderDao.getMostRecentOrderDate(customerId);
    }

    @Override
    public List<Order> findAllOrdersToday(Date dateParam) {
        //List<Order> pendingOrders = this.orderDao.findAllPendingOrders();
        List<Order> todayOrders = this.orderDao.findAllOrdersToday(dateParam);
        List<Order> allOrders = new ArrayList<>();
        //allOrders.addAll(pendingOrders);

        for (Order todayOrder: todayOrders) {
            if (!allOrders.contains(todayOrder)) {
                allOrders.add(todayOrder);
            }
        }

        //allOrders.addAll(todayOrders);
        return allOrders;
    }

    @Override
    public void cancelOrder(long orderId) {
        this.orderDao.cancelOrder(orderId);
    }

    @Override
    public void setToDelivered(long orderId) {
        this.orderDao.setToDelivered(orderId);
    }

    @Override
    public Order findOrderByOrderId(long orderId) {
        return this.orderDao.findById(orderId).orElseGet(Order::new);
    }

    @Override
    public String getNewDrNumber() {
        String newDrNumber = "";

        List<Order> orderList = new ArrayList<>();
        this.orderDao.findAll().forEach(orderList::add);
        List<Integer> drList = new ArrayList<>();

        for (Order order : orderList) {
            drList.add(Integer.valueOf(order.getDeliveryReceiptNum()));
        }

        if (drList == null || drList.size() == 0) {
            newDrNumber = "1";
        } else {
            Integer temp = Collections.max(drList) + 1;
            newDrNumber = temp.toString();

            System.out.println("New DR Number is: " + newDrNumber);
        }

        return newDrNumber;
    }

    @Override
    public List<Order> findAllOrdersPerMonth(Integer month, Integer year) {
        return this.orderDao.findAllOrdersPerMonth(month, year);
    }

    @Override
    public List<Long> inactiveCustomerIds(Date dateVal) {
        return orderDao.inactiveCustomerIds(dateVal);
    }

    @Override
    public Integer getOrderCountPerMonth(Integer month, Integer year, long customerId) {
        return this.orderDao.getOrderCountPerMonth(month, year, customerId);
    }

}
