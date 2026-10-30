package com.spring.aurora.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Stores a numeric String field in an integer column. MySQL coerced these implicitly;
 * PostgreSQL rejects a varchar value for an integer column.
 */
@Converter
public class StringToIntegerConverter implements AttributeConverter<String, Integer> {

    @Override
    public Integer convertToDatabaseColumn(String value) {
        return (value == null ? null : Integer.valueOf(value.trim()));
    }

    @Override
    public String convertToEntityAttribute(Integer value) {
        return (value == null ? null : value.toString());
    }
}
