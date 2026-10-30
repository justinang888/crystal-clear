package com.spring.aurora.entity;

import java.sql.Date;

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
public class CustomerWithPrice {

    private long customerId;

    private String type;

    private String name;

    private String address;

    private String contactName;

    private String mainNumber;

    private String alternateNumber;

    private String emailAddress;

    private int orderInterval;

    private int totalRound;

    private int totalSlim;

    private long productId;

    private Double sellingPrice;
}
