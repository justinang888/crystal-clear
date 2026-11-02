package com.spring.aurora.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReportUtilTest {

    @Test
    void currencyFormatPadsToTwoDecimals() {
        assertThat(ReportUtil.applyCurrencyFormat("360.0")).isEqualTo("360.00");
        assertThat(ReportUtil.applyCurrencyFormat("25.5")).isEqualTo("25.50");
        assertThat(ReportUtil.applyCurrencyFormat("65.25")).isEqualTo("65.25");
    }

    /**
     * The daily sales totals are built with "" + double. Sums of amounts with cents often have
     * long binary tails (55.10 + 60.20 = 115.30000000000001), and from 10 million up Java prints
     * doubles in E notation. The formatter used to drop the cents or the whole amount for these.
     */
    @Test
    void currencyFormatKeepsCentsFromLongDecimalsAndENotation() {
        assertThat(ReportUtil.applyCurrencyFormat("" + (0.1 + 0.2))).isEqualTo("0.30");
        assertThat(ReportUtil.applyCurrencyFormat("" + (55.10 + 60.20))).isEqualTo("115.30");
        assertThat(ReportUtil.applyCurrencyFormat("" + 12_000_000.0)).isEqualTo("12000000.00");
        assertThat(ReportUtil.applyCurrencyFormat("" + 12_345_678.9)).isEqualTo("12345678.90");
        assertThat(ReportUtil.applyCurrencyFormat("100")).isEqualTo("100.00");
        assertThat(ReportUtil.applyCurrencyFormat("" + (0.0 - 25.5))).isEqualTo("-25.50");
        assertThat(ReportUtil.applyCurrencyFormat("0.005")).as("rounds half up").isEqualTo("0.01");
    }

    @Test
    void leapYears() {
        assertThat(ReportUtil.isLeapYear(2028)).isTrue();
        assertThat(ReportUtil.isLeapYear(2026)).isFalse();
        assertThat(ReportUtil.isLeapYear(2000)).isTrue();
        assertThat(ReportUtil.isLeapYear(2100)).isFalse();
    }
}
