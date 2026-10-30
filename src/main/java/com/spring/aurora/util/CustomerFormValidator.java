package com.spring.aurora.util;

import com.spring.aurora.model.Customer;
import com.spring.aurora.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class CustomerFormValidator implements Validator {

    @Autowired
    @Qualifier("emailValidator")
    EmailValidator emailValidator;

    @Override
    public boolean supports(Class<?> aClass) {
        return Customer.class.equals(aClass) || Order.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"name", "NotEmpty.custForm.name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"type", "NotEmpty.custForm.type");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"address", "NotEmpty.custForm.address");
        Customer customer = (Customer) target;
        if (!StringUtils.isEmpty(customer.getEmailAddress())
                && StringUtils.hasText(customer.getEmailAddress())
                && emailValidator.valid(customer.getEmailAddress())){
            errors.reject("email", "Pattern.custForm.email");
        }
    }
}
