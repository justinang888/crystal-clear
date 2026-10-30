package com.spring.aurora.util;

import com.spring.aurora.model.CustomerPrice;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class CustomerPriceFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerPrice.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "customerId", "NotEmpty.customerPriceForm.customerId");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productId", "NotEmpty.customerPriceForm.productId");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellingPrice", "NotEmpty.customerPriceForm.sellingPrice");
        CustomerPrice price = (CustomerPrice) target;
        if (price.getSellingPrice() == null || price.getSellingPrice() < 0) {
            errors.rejectValue("sellingPrice", "Valid.customerPriceForm.sellingPrice");
        }
    }
}
