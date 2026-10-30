package com.spring.aurora.util;

import com.spring.aurora.model.Payment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class PaymentFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Payment.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "remarks", "NotEmpty.paymentForm.remarks");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "NotEmpty.paymentForm.amount");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentType", "NotEmpty.paymentForm.paymentType");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "createdAt", "NotEmpty.paymentForm.createdAt");
        Payment payment = (Payment) target;
        if (payment.getAmount() == null || payment.getAmount() == 0) {
            errors.rejectValue("amount", "Valid.paymentForm.amount");
        }
    }
}
