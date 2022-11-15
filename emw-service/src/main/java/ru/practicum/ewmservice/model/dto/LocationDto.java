package ru.practicum.ewmservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Широта и долгота места проведения события
 */
@Getter
@Setter
@Builder
@ToString
public class LocationDto {

    private Long id;

    private String description; // TODO #Location В текущей версии может быть null.

    @NotNull
    private double lat;

    @NotNull
    private double lon;
}
