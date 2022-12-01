package ru.practicum.ewmservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Данные для добавления новой подборки событий
 */
@Getter
@Setter
@ToString
public class NewCompilationDto {
    private Boolean pinned;
    @NotBlank
    private String title;
    private List<Long> events;
}
