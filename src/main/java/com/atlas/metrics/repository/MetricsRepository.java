package com.atlas.metrics.repository;

import com.atlas.metrics.repository.model.MetricEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricsRepository extends JpaRepository<MetricEventEntity, Long> {}
