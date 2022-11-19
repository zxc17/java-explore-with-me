package ru.practicum.ewmservice.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.customException.ValidationConflictException;
import ru.practicum.ewmservice.customException.ValidationDataException;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.EventMapper;
import ru.practicum.ewmservice.mapper.RequestMapper;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.EventState;
import ru.practicum.ewmservice.model.QEvent;
import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.RequestState;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.AdminUpdateEventRequest;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.model.dto.UpdateEventRequest;
import ru.practicum.ewmservice.storage.CategoryRepository;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.RequestRepository;
import ru.practicum.ewmservice.storage.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EntityManager entityManager;

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

    public ParticipationRequestDto findRequestToEventByUser(Long requesterId, Long eventId) {
        if (!userRepository.existsById(requesterId))
            throw new ValidationNotFoundException(String
                    .format("User with id=%s not found.", requesterId));
        if (!eventRepository.existsById(eventId))
            throw new ValidationNotFoundException(String
                    .format("Event with id=%s not found.", eventId));
        Request request = requestRepository.findByRequester_IdAndEvent_Id(requesterId, eventId);
        return requestMapper.toParticipationRequestDto(request);
    }

    public ParticipationRequestDto confirmRequestByInitiator(Long initiatorId, Long eventId, Long reqId) {
        if (!userRepository.existsById(initiatorId))
            throw new ValidationNotFoundException(String
                    .format("User with id=%s not found.", initiatorId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        Request req = requestRepository.findById(reqId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Request with id=%s not found.", reqId)));
        if (req.getState() != RequestState.PENDING)
            throw new ValidationConflictException(String
                    .format("Request id=%s in status=%s. Confirm is not possible.", reqId, req.getState()));
        long confirmedRequest = 0;
        if (event.getParticipantLimit() > 0) {
            confirmedRequest = requestRepository.countByEvent_IdAndState(eventId, RequestState.CONFIRMED);
            if (confirmedRequest >= event.getParticipantLimit())
                throw new ValidationConflictException("The maximum number of participants has been reached.");
        }
        req.setState(RequestState.CONFIRMED);
        req = requestRepository.save(req);
        // Если достигнут лимит участников, все неподтвержденные заявки отклоняем.
        if (++confirmedRequest == event.getParticipantLimit()) {
            List<Request> requestsToReject = requestRepository.findByEvent_IdAndState(eventId, RequestState.PENDING);
            requestsToReject.forEach(r -> r.setState(RequestState.REJECTED));
            requestRepository.saveAll(requestsToReject);
        }
        return requestMapper.toParticipationRequestDto(req);
    }

    public ParticipationRequestDto rejectRequestByInitiator(Long initiatorId, Long eventId, Long reqId) {
        if (!userRepository.existsById(initiatorId))
            throw new ValidationNotFoundException(String
                    .format("User with id=%s not found.", initiatorId));
        if (!eventRepository.existsById(eventId))
            throw new ValidationNotFoundException(String
                    .format("Event with id=%s not found.", eventId));
        Request req = requestRepository.findById(reqId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Request with id=%s not found.", reqId)));
        if (req.getState() == RequestState.REJECTED)
            throw new ValidationConflictException(String
                    .format("Request id=%s already in status=%s. Reject is not possible.", reqId, req.getState()));
        req.setState(RequestState.REJECTED);
        req = requestRepository.save(req);
        return requestMapper.toParticipationRequestDto(req);
    }

    public List<EventFullDto> findByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                          String rangeStart, String rangeEnd, Integer from, Integer size) {
        List<EventState> eventStates = new ArrayList<>();
        for (String s : states) {
            try {
                eventStates.add(EventState.valueOf(s));
            } catch (IllegalArgumentException e) {
                //NOP
            }
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(rangeStart, dtf);
        } catch (IllegalArgumentException e) {
            throw new ValidationDataException(String.format("Invalid data rangeStart=%s", rangeStart));
        }
        LocalDateTime end;
        try {
            end = LocalDateTime.parse(rangeEnd, dtf);
        } catch (IllegalArgumentException e) {
            throw new ValidationDataException(String.format("Invalid data rangeEnd=%s", rangeEnd));
        }

        Pageable pageable = PageRequest.of(from / size, size);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        List<Predicate> ps = new ArrayList<>();
        if (users != null)
            ps.add(QEvent.event.initiator.id.in(users));
        BooleanExpression byUserId = QEvent.event.initiator.id.in(users);
        BooleanExpression byStates = QEvent.event.state.in(eventStates);
        BooleanExpression byCategories = QEvent.event.category.id.in(categories);
        BooleanExpression byRangeStart = QEvent.event.eventDate.after(start);
        BooleanExpression byRangeEnd = QEvent.event.eventDate.before(end);
        Predicate predicate = byUserId.and(byStates).and(byCategories).and(byRangeStart).and(byRangeEnd);
        predicate = criteriaBuilder.and();
        Page<Event> events = eventRepository.findAll(predicate, pageable);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());

    }

    public EventFullDto updateByAdmin(AdminUpdateEventRequest updateEventDto, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (updateEventDto.getAnnotation() != null)
            event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getCategory() != null)
            event.setCategory(categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new ValidationNotFoundException(String
                            .format("Category with id=%s not found.", updateEventDto.getCategory()))));
        if (updateEventDto.getDescription() != null)
            event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getEventDate() != null)
            event.setEventDate(updateEventDto.getEventDate());
        if (updateEventDto.getLocation() != null)
            event.setLocation(updateEventDto.getLocation());
        if (updateEventDto.getPaid() != null)
            event.setPaid(updateEventDto.getPaid());
        if (updateEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        if (updateEventDto.getRequestModeration() != null)
            event.setRequestModeration(updateEventDto.getRequestModeration());
        if (updateEventDto.getTitle() != null)
            event.setTitle(updateEventDto.getTitle());
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto confirmRequestByAdmin(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (event.getEventDate().minusHours(1).isBefore(LocalDateTime.now()))
            throw new ValidationConflictException(
                    "The event cannot be published less than an hour from the current moment.");
        if (event.getState() != EventState.PENDING)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Confirm is not possible.", eventId, event.getState()));
        event.setState(EventState.PUBLISHED);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto reject(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Reject is not possible.", eventId, event.getState()));
        event.setState(EventState.CANCELED);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }
}
