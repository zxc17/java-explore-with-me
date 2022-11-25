package ru.practicum.ewmservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Широта и долгота места проведения события
 */
@Getter
@Setter
@AllArgsConstructor
public class Location {
    private double lat;
    private double lon;
}
