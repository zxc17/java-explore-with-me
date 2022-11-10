package ru.practicum.ewmservice.model;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
public class Category {
    private Long id;
    private String name;
}
