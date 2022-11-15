package ru.practicum.statsserver.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewStats {
    private String app;
    private String uri;
    private String ip;
    private Long hits;
}
