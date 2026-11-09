package com.spring.aurora.model;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

public interface BankReconciliationRepository extends CrudRepository<BankReconciliation, LocalDate> {
}
