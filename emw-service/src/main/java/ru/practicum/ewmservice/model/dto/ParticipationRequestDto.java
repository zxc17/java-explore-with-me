package ru.practicum.ewmservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

/**
 * Заявка на участие в событии
 */
@Getter
@Setter
@Builder
public class ParticipationRequestDto {
    private Long id;
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private String status;
}
