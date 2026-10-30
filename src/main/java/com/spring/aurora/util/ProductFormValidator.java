package com.spring.aurora.util;

import com.spring.aurora.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ProductFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.productForm.name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "NotEmpty.productForm.description");
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "initialPrice", "NotEmpty.productForm.initialPrice");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellingPrice", "NotEmpty.productForm.sellingPrice");

        Product product = (Product) target;
        /*if (product.getInitialPrice() == null || product.getInitialPrice() <= 0) {
            errors.rejectValue("initialPrice", "Valid.productForm.initialPrice");
        }*/
        if (product.getSellingPrice() == null || product.getSellingPrice() < 0) {
            errors.rejectValue("sellingPrice", "Valid.productForm.sellingPrice");
        }

    }
}
