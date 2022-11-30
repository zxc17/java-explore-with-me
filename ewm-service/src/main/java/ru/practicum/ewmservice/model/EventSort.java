package ru.practicum.ewmservice.model;

public enum EventSort {
    EVENT_DATE,
    VIEWS,
    UNSORTED;

    public static EventSort convert(String sort) {
        try {
            return valueOf(sort.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

}
