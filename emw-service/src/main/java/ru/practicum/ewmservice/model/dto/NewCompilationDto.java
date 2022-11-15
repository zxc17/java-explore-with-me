package ru.practicum.ewmservice.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Данные для добавления новой подборки событий
 */
@Getter
@Setter
public class NewCompilationDto {
    private Boolean pinned;
    private String title;
    private List<Long> events;
}
