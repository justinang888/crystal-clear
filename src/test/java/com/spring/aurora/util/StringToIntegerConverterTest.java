package com.spring.aurora.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StringToIntegerConverterTest {

    private final StringToIntegerConverter converter = new StringToIntegerConverter();

    @Test
    void convertsBothWays() {
        assertThat(this.converter.convertToDatabaseColumn("3")).isEqualTo(3);
        assertThat(this.converter.convertToDatabaseColumn(" 12 ")).isEqualTo(12);
        assertThat(this.converter.convertToEntityAttribute(7)).isEqualTo("7");
    }

    @Test
    void keepsNull() {
        assertThat(this.converter.convertToDatabaseColumn(null)).isNull();
        assertThat(this.converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void rejectsNonNumbersLikeTheIntegerColumnWould() {
        assertThatThrownBy(() -> this.converter.convertToDatabaseColumn("two"))
                .isInstanceOf(NumberFormatException.class);
    }
}
