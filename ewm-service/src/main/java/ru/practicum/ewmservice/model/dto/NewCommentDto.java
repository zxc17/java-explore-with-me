package ru.practicum.ewmservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString
public class NewCommentDto {

    @NotNull
    Long event;

    @NotBlank
    private String text;
}
