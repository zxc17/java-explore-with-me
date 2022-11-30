package ru.practicum.statsserver.mapper;

import ru.practicum.statsserver.model.StatsRecord;
import ru.practicum.statsserver.model.dto.EndpointHit;

public class StatsMapper {

    public static StatsRecord toStatRecord(EndpointHit hit) {
        return StatsRecord.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

}
