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
     * KNOWN BUG (pinned): the daily sales totals are built with "" + double. Sums of amounts
     * with cents often have long binary tails (0.1 + 0.2 = 0.30000000000000004), and from
     * 10 million up Java prints doubles in E notation. Both lose the cents or the whole amount.
     */
    @Test
    void currencyFormatDropsCentsFromLongDecimalsAndBreaksOnENotation() {
        assertThat(ReportUtil.applyCurrencyFormat("" + (0.1 + 0.2))).isEqualTo("0");
        assertThat(ReportUtil.applyCurrencyFormat("" + (55.10 + 60.20))).isEqualTo("115");
        assertThat(ReportUtil.applyCurrencyFormat("" + 12_000_000.0)).isEqualTo("1");
        assertThat(ReportUtil.applyCurrencyFormat("100")).isEqualTo(".00");
    }

    @Test
    void leapYears() {
        assertThat(ReportUtil.isLeapYear(2028)).isTrue();
        assertThat(ReportUtil.isLeapYear(2026)).isFalse();
        assertThat(ReportUtil.isLeapYear(2000)).isTrue();
        assertThat(ReportUtil.isLeapYear(2100)).isFalse();
    }
}
