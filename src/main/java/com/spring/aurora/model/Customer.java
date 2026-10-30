package com.spring.aurora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.spring.aurora.entity.CustomerWithPrice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="CUSTOMER")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_id")
    private long customerId;

    @Column(name="type")
    private String type;

    @Column(name="name")
    private String name;

    @Column(name="address")
    private String address;

    @Column(name="contact_name")
    private String contactName;

    @Column(name="main_number")
    private String mainNumber;

    @Column(name="alternate_number")
    private String alternateNumber;

    @Column(name="email_address")
    private String emailAddress;

    @Column(name="order_interval")
    private int orderInterval;

    @Column(name="total_round")
    private int totalRound;

    @Column(name="total_slim")
    private int totalSlim;

    public boolean isNew() {
        return (this.customerId == 0);
    }

    public Customer(CustomerWithPrice cp) {
        super();
        this.customerId = cp.getCustomerId();
        this.type = cp.getType();
        this.name = cp.getName();
        this.address = cp.getAddress();
        this.contactName = cp.getContactName();
        this.mainNumber = cp.getMainNumber();
        this.alternateNumber = cp.getAlternateNumber();
        this.emailAddress = cp.getEmailAddress();
        this.orderInterval = cp.getOrderInterval();
        this.totalRound = cp.getTotalRound();
        this.totalSlim = cp.getTotalSlim();
    }
    
}   
