package ru.practicum.statsserver.storage;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import ru.practicum.statsserver.model.dto.ViewStats;

import java.util.List;

public interface StatsRepositoryCustom {
    List<ViewStats> findHits(BooleanBuilder criteria, NumberExpression<Long> hits);
}
