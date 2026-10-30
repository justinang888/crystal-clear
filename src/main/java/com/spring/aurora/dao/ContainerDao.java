package com.spring.aurora.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.spring.aurora.model.Container;

@Repository
public interface ContainerDao extends CrudRepository<Container, Long> {

    List<Container> findAllByCustomerId(long customerId);

    @Query("select c from Container c where cast(c.createdAt as date) = :date")
    List<Container> findContainerActivityByDate(Date date);

}
