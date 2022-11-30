package ru.practicum.statsserver.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.statsserver.model.StatsRecord;

public interface StatsRepository extends
        JpaRepository<StatsRecord, Long>,
        QuerydslPredicateExecutor<StatsRecord>,
        StatsRepositoryCustom {

}
