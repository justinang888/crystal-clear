package com.spring.aurora.entity;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class CustomerWithOrder {

    private long customerId;

    private String type;

    private String name;

    private String address;

    private String contactName;

    private String mainNumber;

    private String emailAddress;

    private long orderId;
    
    //private Timestamp mostRecentOrderDate;
    
    //private int numberOfOrdersForTheMonth;
    
}
