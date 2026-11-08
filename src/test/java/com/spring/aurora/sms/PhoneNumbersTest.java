package com.spring.aurora.sms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Numbers are stored as staff typed them; texts need +639XXXXXXXXX. */
class PhoneNumbersTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "09170000001", "0917 000 0001", "0917-000-0001", "(0917) 000-0001", "  0917 000 0001  ",
            "9170000001", "639170000001", "+639170000001", "+63 917 000 0001", "+63-917-000-0001",
            "0917.000.0001" })
    void mobileNumbersInAnyCommonShapeBecomeInternational(String typed) {
        assertThat(PhoneNumbers.toMobile(typed)).contains("+639170000001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "(02) 8123 4567",   // Manila landline
            "028123 4567",
            "8123-4567",        // landline without area code
            "0917 000 000",     // a digit short
            "0917 000 00011",   // a digit long
            "08170000001",      // 08 is not a mobile prefix
            "+1 917 000 0001",  // not a Philippine number
            "N/A", "none", "0917 000 0001 / 0918 000 0002" })
    void anythingElseIsNotAMobileNumber(String typed) {
        assertThat(PhoneNumbers.toMobile(typed)).isEmpty();
    }
}
