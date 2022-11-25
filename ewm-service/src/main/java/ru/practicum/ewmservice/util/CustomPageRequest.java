package ru.practicum.ewmservice.util;

import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


public class CustomPageRequest extends PageRequest {
    private final long offset;

    private CustomPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        offset = from;
    }

    public static CustomPageRequest of(int from, int size, @NonNull Sort sort) {
        return new CustomPageRequest(from, size, sort);
    }

    public static CustomPageRequest of(int from, int size) {
        return new CustomPageRequest(from, size, Sort.unsorted());
    }

    @Override
    public long getOffset() {
        return offset;
    }
}
