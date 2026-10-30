package com.spring.aurora.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.spring.aurora.model.Customer;
import com.spring.aurora.model.Order;

@Component
public class OrderFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Order.class.equals(aClass) || Customer.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "deliveryReceiptNum", "NotEmpty.orderForm.deliveryReceiptNum");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amountPaid", "NotEmpty.orderForm.amountPaid");
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalAmount", "NotEmpty.orderForm.totalAmount");
        Order order = (Order) target;
        if (order.getTotalAmount() == null || order.getTotalAmount() < 0){
            errors.rejectValue("totalAmount", "Valid.orderForm.totalAmount");
        }
    }
}
