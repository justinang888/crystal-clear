package com.spring.aurora.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales for one day, week or month on the dashboard chart: the first and last day of the
 * period (both included) and the delivered orders' total, in pesos.
 */
public record SalesPeriod(LocalDate start, LocalDate end, BigDecimal total) {
}
