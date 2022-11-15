package ru.practicum.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.customException.ValidationConflictException;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.EventMapper;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.EventState;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.model.dto.UpdateEventRequest;
import ru.practicum.ewmservice.storage.CategoryRepository;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public List<EventShortDto> findAllByCurrentUser(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String.format("User with id=%s not found.", userId));
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiator_Id(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto update(UpdateEventRequest updateEventRequest, Long userId) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String.format("User with id=%s not found.", userId));
        Event event = eventRepository.findById(updateEventRequest.getEventId())
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", updateEventRequest.getEventId())));
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of event id=%s", userId, event.getId()));
        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Can't edit events in status=%s", event.getState()));

        if (updateEventRequest.getAnnotation() != null)
            event.setAnnotation(updateEventRequest.getAnnotation());
        if (updateEventRequest.getCategoryId() != null)
            event.setCategory(categoryRepository.findById(updateEventRequest.getCategoryId())
                    .orElseThrow(() -> new ValidationNotFoundException(String
                            .format("Category with id=%s not found.", updateEventRequest.getCategoryId()))));
        if (updateEventRequest.getDescription() != null)
            event.setDescription(updateEventRequest.getDescription());
        LocalDateTime newEventDate = updateEventRequest.getEventDate();
        if (newEventDate != null) {
            if (newEventDate.minusHours(2).isBefore(LocalDateTime.now()))
                throw new ValidationConflictException(
                        "Event time cannot be set less than 2 hours from the current moment.");
            event.setEventDate(newEventDate);
        }
        if (updateEventRequest.getPaid() != null)
            event.setPaid(updateEventRequest.getPaid());
        if (updateEventRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        if (updateEventRequest.getTitle() != null)
            event.setTitle(updateEventRequest.getTitle());
        if (event.getState() == EventState.CANCELED)
            event.setState(EventState.PENDING);

        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto add(NewEventDto newEventDto, Long userId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("User with id=%s not found.", userId)));
        Event event = eventMapper.toEvent(newEventDto);
        if (event.getEventDate().minusHours(2).isBefore(LocalDateTime.now()))
            throw new ValidationConflictException(
                    "Event time cannot be set less than 2 hours from the current moment.");
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setViews(0L);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto findById(Long userId, Long eventId) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String
                    .format("User with id=%s not found.", userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of the event id=%s", userId, eventId));
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto cancel(Long userId, Long eventId) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String
                    .format("User with id=%s not found.", userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of the event id=%s", userId, eventId));
        if (event.getState() != EventState.PENDING)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Cancel is not possible.", eventId, event.getState()));
        event.setState(EventState.CANCELED);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }
}
