package com.spring.aurora.sms;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Turns a number as staff typed it into the form a text is sent to. Only Philippine mobile
 * numbers (09XX XXX XXXX) can receive texts here; landlines and anything unclear are left out
 * rather than guessed. Stored numbers are never changed.
 */
public final class PhoneNumbers {

    /** Spaces, dashes, dots and brackets people type between digit groups. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-.()]");
    /** The ten digits after the country code: 9 and nine more. */
    private static final Pattern MOBILE = Pattern.compile("9\\d{9}");

    private PhoneNumbers() {
    }

    /** "0917 000 0001" -> "+639170000001"; empty when it is not a Philippine mobile number. */
    public static Optional<String> toMobile(String typed) {
        if (typed == null) {
            return Optional.empty();
        }
        String digits = SEPARATORS.matcher(typed).replaceAll("");
        if (digits.startsWith("+63")) {
            digits = digits.substring(3);
        } else if (digits.startsWith("63") && digits.length() == 12) {
            digits = digits.substring(2);
        } else if (digits.startsWith("0") && digits.length() == 11) {
            digits = digits.substring(1);
        }
        return MOBILE.matcher(digits).matches() ? Optional.of("+63" + digits) : Optional.empty();
    }
}
