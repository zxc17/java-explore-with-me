package ru.practicum.ewmservice.model.dto;

import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewmservice.model.Location;

import java.time.LocalDateTime;

public class AdminUpdateEventRequest {
    private String annotation;
    private Long category;
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;
}
