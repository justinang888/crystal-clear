package com.spring.aurora.service;

import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.ExpenseDao;
import com.spring.aurora.model.Expense;

@Service("expenseService")
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseDao expenseDao;

    public void setExpenseDao(ExpenseDao dao) {
        this.expenseDao = dao;
    }

    @Override
    public Expense save(Expense expense) {
        return this.expenseDao.save(expense);
    }

    @Override
    public List<Expense> findAllByDate(Date date) {
        return this.expenseDao.findAllByCreatedAt(date);
    }

    @Override
    public double getTotalExpensesByDate(Date date) {
        List<Expense> expenses = this.expenseDao.findAllByCreatedAt(date);
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        return total;
    }
}
