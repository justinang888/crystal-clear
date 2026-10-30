package com.spring.aurora.service;

import com.spring.aurora.model.Expense;

import java.sql.Date;
import java.util.List;

public interface ExpenseService {
    Expense save(Expense expense);
    List<Expense> findAllByDate(Date date);
    double getTotalExpensesByDate(Date date);
}
