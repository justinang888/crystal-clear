package com.spring.aurora.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.Expense;

public interface ExpenseDao extends CrudRepository<Expense, Long> {
    List<Expense> findAllByCreatedAt(Date createdAt);

    /** Expenses on any day from {@code from} to {@code to}, both included, whatever their time of day. */
    @Query("select e from Expense e where cast(e.createdAt as date) between :from and :to")
    List<Expense> findAllBetween(Date from, Date to);
}
