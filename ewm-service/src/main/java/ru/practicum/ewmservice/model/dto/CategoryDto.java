package ru.practicum.ewmservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Категория
 */
@Getter
@Setter
@Builder
@ToString
public class CategoryDto {

    @NotNull
    @Positive
    private Long id;

    @NotBlank
    private String name;
}

