package ru.practicum.ewmservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.model.Location;
import ru.practicum.ewmservice.model.dto.LocationDto;

@Component
public class LocationMapper {

    public Location toLocation(LocationDto locationDto) {
        return Location.builder()
                .description(locationDto.getDescription())
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .description(location.getDescription())
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
