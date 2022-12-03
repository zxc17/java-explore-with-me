package ru.practicum.ewmservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

@Getter
@Setter
@Builder
@ToString
public class CommentDto {

    private Long id;
    private Long eventId;
    private UserDto commentator;
    private String text;
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime created;
}
