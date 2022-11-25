package ru.practicum.statsserver.storage;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsserver.model.QStatsRecord;
import ru.practicum.statsserver.model.dto.ViewStats;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryCustomImpl implements StatsRepositoryCustom {
    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> findHits(BooleanBuilder findCriteria, NumberExpression<Long> hits) {
        QStatsRecord qStats = QStatsRecord.statsRecord;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory.select(Projections.constructor(ViewStats.class, qStats.app, qStats.uri, hits))
                .from(qStats)
                .where(findCriteria)
                .groupBy(qStats.app)
                .groupBy(qStats.uri)
                .fetch();
    }
}
