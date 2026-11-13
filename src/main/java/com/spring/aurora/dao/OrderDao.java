package com.spring.aurora.dao;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Order;

public interface OrderDao extends CrudRepository<Order, Long> {
    List<Order> findAllByCustomerId(long customerId);

    @Query("select o from Order o where cast(o.createdAt as date) = :timestamp")
    List<Order> findAllOrdersToday(Date timestamp);

    /** Orders on any day from {@code from} to {@code to}, both included, whatever their time of day. */
    @Query("select o from Order o where cast(o.createdAt as date) between :from and :to")
    List<Order> findAllOrdersBetween(java.sql.Date from, java.sql.Date to);

    /** Delivered orders from {@code from} up to, but not including, {@code until}. */
    @Query("select o from Order o where lower(o.status) = 'delivered' and o.createdAt >= :from and o.createdAt < :until")
    List<Order> findDeliveredBetween(Timestamp from, Timestamp until);

    /** Each customer's last delivered order time, as [customerId, createdAt] rows; never-delivered customers have none. */
    @Query("select o.customerId, max(o.createdAt) from Order o where lower(o.status) = 'delivered' group by o.customerId")
    List<Object[]> findLastDeliveredPerCustomer();

    /** Every pending order, from any day, oldest first; older rows may spell the status in another case. */
    @Query("select o from Order o where lower(o.status) = 'pending' order by o.orderId")
    List<Order> findAllPendingOrders();

    @Query("select o from Order o where MONTH(o.createdAt) = :month and YEAR(o.createdAt) = :year")
    List<Order> findAllOrdersPerMonth(Integer month, Integer year);

    @Query("select MAX(o.createdAt) from Order o where o.customerId = :customerId")
    Timestamp getMostRecentOrderDate(long customerId);
    
    @Query("select count(o) from Order o where MONTH(o.createdAt) = :month and YEAR(o.createdAt) = :year and o.customerId = :customerId")
    Integer getOrderCountPerMonth(Integer month, Integer year, long customerId);

    @Modifying
    @Query("update Order o set o.status = 'Cancelled' where o.orderId = :orderId")
    void cancelOrder(long orderId);

    @Modifying
    @Query("update Order o set o.status = 'Delivered', o.createdAt = current_timestamp where o.orderId = :orderId")
    void setToDelivered(long orderId);

    // List<Order> findAllByDeliveryReceiptNumber(int drNumber);
    // Order findOrderByOrderId(long orderId);
    // String getNewDrNumber();
    
    @Query("select c.customerId FROM Customer c, Order o WHERE c.customerId = o.customerId and o.createdAt > :dateVal")
    List<Long> inactiveCustomerIds(Date dateVal);
}
