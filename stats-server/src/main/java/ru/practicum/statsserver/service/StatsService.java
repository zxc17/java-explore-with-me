package ru.practicum.statsserver.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsserver.customException.ValidationDataException;
import ru.practicum.statsserver.mapper.StatsMapper;
import ru.practicum.statsserver.model.QStatsRecord;
import ru.practicum.statsserver.model.dto.EndpointHit;
import ru.practicum.statsserver.model.dto.ViewStats;
import ru.practicum.statsserver.storage.StatsRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.statsserver.util.Constants.DATE_PATTERN;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    public void hit(EndpointHit hit) {
        statsRepository.save(statsMapper.toStatRecord(hit));
    }

    public List<ViewStats> findAll(String start, String end, String app, List<String> uris, Boolean unique) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateTime startRange;
        LocalDateTime endRange;
        try {
            startRange = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), dtf);
        } catch (IllegalArgumentException e) {
            throw new ValidationDataException(String.format("Invalid data start=%s", start));
        }
        try {
            endRange = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), dtf);
        } catch (IllegalArgumentException e) {
            throw new ValidationDataException(String.format("Invalid data end=%s", end));
        }
        QStatsRecord qStats = QStatsRecord.statsRecord;
        BooleanBuilder findCriteria = new BooleanBuilder();
        findCriteria.and(qStats.timestamp.between(startRange, endRange))
                .and(qStats.timestamp.before(endRange));
        if (app != null)
            findCriteria.and(qStats.app.eq(app));
        if (uris != null && uris.size() > 0)
            findCriteria.and(qStats.uri.in(uris));
        NumberExpression<Long> hits = unique ?
                qStats.ip.countDistinct().as("hits") :
                qStats.ip.count().as("hits");
        return statsRepository.findHits(findCriteria, hits);
    }
}
