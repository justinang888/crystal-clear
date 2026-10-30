package com.spring.aurora.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArSummaryRepository extends CrudRepository<ArSummary, Long> {

    @Override
    List<ArSummary> findAll();

    Optional<ArSummary> findByCustomerId(long customerId);
}
