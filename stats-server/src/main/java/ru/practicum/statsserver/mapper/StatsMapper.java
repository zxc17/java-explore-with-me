package ru.practicum.statsserver.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.statsserver.model.StatsRecord;
import ru.practicum.statsserver.model.dto.EndpointHit;

@Component
@RequiredArgsConstructor
public class StatsMapper {

    public StatsRecord toStatRecord(EndpointHit hit) {
        return StatsRecord.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

}
