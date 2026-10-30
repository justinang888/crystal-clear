package com.spring.aurora.util;

import com.spring.aurora.model.Expense;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ExpenseFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Expense.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "NotEmpty.expenseForm.description");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "NotEmpty.expenseForm.amount");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "createdAt", "NotEmpty.expenseForm.createdAt");

        Expense expense = (Expense) target;
        if (expense.getAmount() == null || expense.getAmount() <= 0) {
            errors.rejectValue("amount", "Valid.expenseForm.amount");
        }
    }
}
