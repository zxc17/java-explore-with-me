package ru.practicum.statsserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statsserver.model.dto.EndpointHit;
import ru.practicum.statsserver.model.dto.ViewStats;
import ru.practicum.statsserver.service.StatsService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public void hit(@RequestBody EndpointHit endpointHit) {
        log.info("Statistics еndpoint 'Save hit'. " +
                "endpointHit={}.", endpointHit);
        statsService.hit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> findAll(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String app,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") Boolean unique
    ) {
        log.info("Statistics еndpoint 'Get statistics'. " +
                "start={}, end={}, app={}, uris={}, unique={}.", start, end, app, uris, unique);
        return statsService.findAll(start, end, app, uris, unique);
    }
}
