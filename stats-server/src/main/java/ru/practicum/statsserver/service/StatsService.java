package ru.practicum.statsserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsserver.model.dto.EndpointHit;

@Service
@RequiredArgsConstructor
public class StatsService {

    public void hit(EndpointHit endpointHit) {
    }
}
