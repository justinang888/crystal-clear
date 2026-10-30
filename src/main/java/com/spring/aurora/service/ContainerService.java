package com.spring.aurora.service;

import java.sql.Date;
import java.util.List;

import com.spring.aurora.model.Container;

public interface ContainerService {
    Container insert(Container container);
    Container update(Container container);
    Container delete(Container container);

    List<Container> findAllByCustomerId(long customerId);
    List<Container> findAll();
    List<Container> findContainerActivityByDate(Date date);
}
