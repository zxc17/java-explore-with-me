package ru.practicum.ewmservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.Location;
import ru.practicum.ewmservice.model.RequestState;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.storage.CategoryRepository;
import ru.practicum.ewmservice.storage.LocationRepository;
import ru.practicum.ewmservice.storage.RequestRepository;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public Event toEvent(NewEventDto newEventDto) {
        // TODO #Location
        //  В текущей версии программы нет выбора локации из существующего списка.
        //  Я решил, что будет лучше сделать задел на возможное улучшение,
        //  сразу завести сущность и таблицу для хранения локации.
        //  В текущей версии при создании события всегда создается новая локация.
        Location location = locationMapper.toLocation(newEventDto.getLocation());
        location = locationRepository.save(location);

        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(categoryRepository.findById(newEventDto.getCategory())
                        .orElseThrow(() -> new ValidationNotFoundException(String
                                .format("Category with id=%s not found.", newEventDto.getCategory()))))
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(location)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(requestRepository.countByEvent_IdAndState(event.getId(), RequestState.CONFIRMED))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .location(locationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(requestRepository.countByEvent_IdAndState(event.getId(), RequestState.CONFIRMED))
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }
}