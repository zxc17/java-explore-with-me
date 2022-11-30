package ru.practicum.ewmservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Полная информация о пользователе.
 */
@Getter
@Setter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String name;
}
