package ru.practicum.statsserver.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static ru.practicum.statsserver.util.Constants.DATE_PATTERN;

@Getter
@Setter
public class EndpointHit {

    private Long id;

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @NotBlank
    private String ip;

    @DateTimeFormat(pattern = DATE_PATTERN)
    private LocalDateTime timestamp;
}
