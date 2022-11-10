package ru.practicum.statsserver.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ViewStats {
    private String app;
    private String uri;
    private String ip;
    private Long hits;
}
