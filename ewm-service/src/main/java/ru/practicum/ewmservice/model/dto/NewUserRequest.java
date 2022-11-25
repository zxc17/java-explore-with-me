package ru.practicum.ewmservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Данные для добавления нового юзера
 */
@Getter
@Setter
@ToString
public class NewUserRequest {

    @NotNull
    @Email
    private String email;

    @NotBlank
    private String name;
}
