package ru.practicum.ewmservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Краткая информация о пользователе.
 */
@Getter
@Setter
@Builder
public class UserShortDto {
    private Long id;
    private String name;
}
