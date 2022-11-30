package ru.practicum.ewmservice.model;

import java.util.ArrayList;
import java.util.List;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState convert(String state) {
        try {
            return valueOf(state.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public static List<EventState> convertList(List<String> states) {
        List<EventState> result = new ArrayList<>();
        for (String s : states) {
            EventState state = convert(s);
            if (state != null) result.add(state);
        }
        return result;
    }

}
