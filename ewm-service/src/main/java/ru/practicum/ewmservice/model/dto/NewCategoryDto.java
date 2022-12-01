package ru.practicum.ewmservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * Данные для добавления новой категории
 */
@Getter
@Setter
@ToString
public class NewCategoryDto {
    @NotBlank
    private String name;
}

