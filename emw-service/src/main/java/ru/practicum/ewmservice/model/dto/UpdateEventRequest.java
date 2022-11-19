package ru.practicum.ewmservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

/**
 * Данные для изменения информации о событии
 */
@Getter
@Setter
@ToString
public class UpdateEventRequest {
    @NotNull
    private Long eventId;
    @Min(value = 20) @Max(value = 2000)
    private String annotation;
    private Long categoryId;
    @Min(value = 20) @Max(value = 7000)
    private String description;
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer participantLimit;
    @Min(value = 3) @Max(value = 120)
    private String title;
}
