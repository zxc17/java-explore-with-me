package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.Location;
import ru.practicum.ewmservice.model.dto.NewEventDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .locationLat(newEventDto.getLocation().getLat())
                .locationLon(newEventDto.getLocation().getLon())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public static List<EventFullDto> toEventFullDto(Iterable<Event> events, Map<Long, Long> views) {
        List<EventFullDto> result = new ArrayList<>();
        events.forEach(e -> result.add(toEventFullDto(e, views)));
        return result;
    }

    public static EventFullDto toEventFullDto(Event event, Map<Long, Long> views) {
        long confirmedMembers = Optional.ofNullable(event.getConfirmedMembers()).orElse(new ArrayList<>()).size();
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedMembers)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(new Location(event.getLocationLat(), event.getLocationLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(Optional.ofNullable(views.get(event.getId())).orElse(0L))
                .build();
    }

    public static List<EventShortDto> toEventShortDto(List<Event> events, Map<Long, Long> views) {
        return events.stream()
                .map(e -> toEventShortDto(e, views))
                .collect(Collectors.toList());
    }

    public static EventShortDto toEventShortDto(Event event, Map<Long, Long> views) {
        long confirmedMembers = Optional.ofNullable(event.getConfirmedMembers()).orElse(new ArrayList<>()).size();
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedMembers)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(Optional.ofNullable(views.get(event.getId())).orElse(0L))
                .build();
    }
}