package com.spring.aurora.sms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Who is offered a text: their last delivered order is more than their usual order interval plus
 * 2 days ago. A blank or zero interval counts as 30 days. Never delivered: not offered.
 */
class OverdueCustomersTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

    @Test
    void aWeeklyCustomerIsOverdueAfterNineDays() {
        // 7 + 2 = 9 days of grace: on day 9 they are not yet overdue, on day 10 they are.
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(9), 7, TODAY)).isFalse();
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(10), 7, TODAY)).isTrue();
    }

    @Test
    void theRuleFollowsEachCustomersOwnInterval() {
        LocalDate twentyDaysAgo = TODAY.minusDays(20);
        assertThat(OverdueCustomers.isOverdue(twentyDaysAgo, 14, TODAY)).as("every 14 days").isTrue();
        assertThat(OverdueCustomers.isOverdue(twentyDaysAgo, 18, TODAY)).as("every 18 days").isFalse();
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(33), 30, TODAY)).as("monthly, day 33").isTrue();
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(32), 30, TODAY)).as("monthly, day 32").isFalse();
    }

    @Test
    void aMissingIntervalCountsAsThirtyDays() {
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(32), 0, TODAY)).isFalse();
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(33), 0, TODAY)).isTrue();
        assertThat(OverdueCustomers.isOverdue(TODAY.minusDays(33), -5, TODAY)).isTrue();
    }

    @Test
    void customersWhoNeverHadADeliveryAreNotOffered() {
        assertThat(OverdueCustomers.isOverdue(null, 7, TODAY)).isFalse();
    }

    @Test
    void aDeliveryTodayOrInTheFutureIsNeverOverdue() {
        assertThat(OverdueCustomers.isOverdue(TODAY, 0, TODAY)).isFalse();
        assertThat(OverdueCustomers.isOverdue(TODAY.plusDays(1), 1, TODAY)).isFalse();
    }

    @Test
    void daysSinceCountsCalendarDays() {
        assertThat(OverdueCustomers.daysSince(TODAY.minusDays(10), TODAY)).isEqualTo(10);
        assertThat(OverdueCustomers.daysSince(TODAY, TODAY)).isZero();
    }
}
