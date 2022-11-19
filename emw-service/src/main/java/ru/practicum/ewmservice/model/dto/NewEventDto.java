package ru.practicum.ewmservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

/**
 * Данные для добавления нового события
 */
@Getter
@Setter
@ToString
public class NewEventDto {

    @NotBlank
    @Min(value = 20)
    @Max(value = 2000)
    private String annotation;

    @NotNull
    @Positive
    private Long category;

    @NotBlank
    @Min(value = 20)
    @Max(value = 7000)
    private String description;

    @NotNull
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank
    @Min(value = 3)
    @Max(value = 120)
    private String title;
}
