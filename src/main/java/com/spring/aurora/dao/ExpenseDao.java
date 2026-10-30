package com.spring.aurora.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Expense;

public interface ExpenseDao extends CrudRepository<Expense, Long> {
    List<Expense> findAllByCreatedAt(Date createdAt);
}
