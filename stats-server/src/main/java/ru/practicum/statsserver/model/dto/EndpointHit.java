package ru.practicum.statsserver.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static ru.practicum.statsserver.util.Constants.DATE_PATTERN;

@Getter
@Setter
@ToString
public class EndpointHit {

    private Long id;

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @NotBlank
    private String ip;

    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime timestamp;
}
