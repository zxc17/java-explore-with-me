package ru.practicum.ewmservice.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EndpointHit {
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;

    public EndpointHit(String app, String uri, String ip) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
        this.timestamp = LocalDateTime.now();
    }
}
