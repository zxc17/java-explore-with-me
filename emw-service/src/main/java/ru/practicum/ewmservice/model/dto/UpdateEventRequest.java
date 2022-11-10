package ru.practicum.ewmservice.model.dto;

import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

public class UpdateEventRequest {
    private Long eventId;
    @Min(value = 20) @Max(value = 2000)
    private String annotation;
    private Long category;
    @Min(value = 20) @Max(value = 7000)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer participantLimit;
    @Min(value = 3) @Max(value = 120)
    private String title;
}
