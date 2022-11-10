package ru.practicum.ewmservice.model.dto;

import java.util.List;

public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
