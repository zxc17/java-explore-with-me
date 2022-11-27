package ru.practicum.ewmservice.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.client.StatsClient;
import ru.practicum.ewmservice.customException.ValidationConflictException;
import ru.practicum.ewmservice.customException.ValidationDataException;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.EventMapper;
import ru.practicum.ewmservice.mapper.RequestMapper;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.EventSort;
import ru.practicum.ewmservice.model.EventState;
import ru.practicum.ewmservice.model.QEvent;
import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.RequestState;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.AdminUpdateEventRequest;
import ru.practicum.ewmservice.model.dto.EndpointHit;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.model.dto.UpdateEventRequest;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.RequestRepository;
import ru.practicum.ewmservice.util.CustomPageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final StatsClient statsClient;
    @Value("${time-before-event}") int tbe;
    @Value("${time-before-event.admin}") int tbeForAdmin;

    public List<EventFullDto> findAll(String text,
                                      List<Long> categories,
                                      Boolean paid,
                                      String rangeStart,
                                      String rangeEnd,
                                      Boolean onlyAvailable,
                                      String sortStr,
                                      Integer from,
                                      Integer size) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
        QEvent qEvent = QEvent.event;
        BooleanBuilder findCriteria = new BooleanBuilder();

        if (text != null) {
            findCriteria.and(qEvent.annotation.containsIgnoreCase(text)
                    .or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categories != null && categories.size() > 0)
            findCriteria.and(qEvent.category.id.in(categories));
        if (paid != null)
            findCriteria.and(qEvent.paid.eq(paid));
        if (rangeStart != null) {
            LocalDateTime start;
            try {
                start = LocalDateTime.parse(rangeStart, dtf);
            } catch (IllegalArgumentException e) {
                throw new ValidationDataException(String.format("Invalid data rangeStart=%s", rangeStart));
            }
            findCriteria.and(qEvent.eventDate.after(start));
        } else {
            findCriteria.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        if (rangeEnd != null) {
            LocalDateTime end;
            try {
                end = LocalDateTime.parse(rangeEnd, dtf);
            } catch (IllegalArgumentException e) {
                throw new ValidationDataException(String.format("Invalid data rangeEnd=%s", rangeEnd));
            }
            findCriteria.and(qEvent.eventDate.before(end));
        }
        if (Boolean.TRUE.equals(onlyAvailable))
            findCriteria.and(qEvent.participantLimit.gt(qEvent.confirmedMembers.size()));
        List<EventFullDto> result;
        switch (Optional.ofNullable(EventSort.convert(sortStr)).orElse(EventSort.UNSORTED)) {
            case EVENT_DATE:
                Sort sort = Sort.by("eventDate");
                Pageable pageableDate = CustomPageRequest.of(from, size, sort);
                result = eventRepository.findAll(findCriteria, pageableDate).stream()
                        .map(eventMapper::toEventFullDto)
                        .collect(Collectors.toList());
                break;
            case VIEWS:
                result = StreamSupport.stream(eventRepository.findAll(findCriteria).spliterator(), false)
                        .map(eventMapper::toEventFullDto)
                        .sorted(Comparator.comparing(EventFullDto::getViews))
                        .skip(from)
                        .limit(size)
                        .collect(Collectors.toList());
                break;
            default: //Несортированный
                Pageable pageableUnsorted = CustomPageRequest.of(from, size);
                result = eventRepository.findAll(findCriteria, pageableUnsorted).stream()
                        .map(eventMapper::toEventFullDto)
                        .collect(Collectors.toList());
        }
        return result;
    }

    public EventFullDto findById(Long eventId, String uri, String ip) {
        Event event = getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Event id=%s not published", eventId));
        EventFullDto result = eventMapper.toEventFullDto(event);
        EndpointHit hit = new EndpointHit("ewm", uri, ip);
        statsClient.sendHit(hit);
        return result;
    }

    public List<EventShortDto> findAllPrivate(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = CustomPageRequest.of(from, size);
        return eventRepository.findByInitiator_Id(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto update(UpdateEventRequest updateEventRequest, Long userId) {
        userService.getUserById(userId);
        Event event = getEventById(updateEventRequest.getEventId());
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of event id=%s", userId, event.getId()));
        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Can't edit events in status=%s", event.getState()));

        if (updateEventRequest.getAnnotation() != null)
            event.setAnnotation(updateEventRequest.getAnnotation());
        if (updateEventRequest.getCategoryId() != null)
            event.setCategory(categoryService.getCategoryById(updateEventRequest.getCategoryId()));
        if (updateEventRequest.getDescription() != null)
            event.setDescription(updateEventRequest.getDescription());
        LocalDateTime newEventDate = updateEventRequest.getEventDate();
        if (newEventDate != null) {
            if (newEventDate.minusHours(tbe).isBefore(LocalDateTime.now()))
                throw new ValidationConflictException(String
                        .format("Event time cannot be set less than %s hour(s) from the current moment.", tbe));
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

    @Transactional
    public EventFullDto add(NewEventDto newEventDto, Long userId) {
        User initiator = userService.getUserById(userId);
        Event event = eventMapper.toEvent(newEventDto);
        if (event.getEventDate().minusHours(tbe).isBefore(LocalDateTime.now()))
            throw new ValidationConflictException(String
                    .format("Event time cannot be set less than %s hour(s) from the current moment.", tbe));
        event.setCreatedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    public EventFullDto findByIdPrivate(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of the event id=%s", userId, eventId));
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto cancel(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
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

    public List<ParticipationRequestDto> findRequestsToEvent(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of the event id=%s", userId, eventId));
        return requestRepository.findByEvent_Id(eventId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto confirmRequestByInitiator(Long userId, Long eventId, Long reqId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!userId.equals(event.getInitiator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of the event id=%s", userId, eventId));
        Request req = requestService.getRequestById(reqId);
        if (req.getState() != RequestState.PENDING)
            throw new ValidationConflictException(String
                    .format("Request id=%s in status=%s. Confirm is not possible.", reqId, req.getState()));
        long confirmedRequest = event.getConfirmedMembers().size();
        if (event.getParticipantLimit() > 0 && confirmedRequest >= event.getParticipantLimit())
            throw new ValidationConflictException("The maximum number of participants has been reached.");
        req.setState(RequestState.CONFIRMED);
        req = requestRepository.save(req);
        // Если достигнут лимит участников, все неподтвержденные заявки отклоняем.
        if (event.getParticipantLimit() > 0 && ++confirmedRequest == event.getParticipantLimit()) {
            List<Request> requestsToReject = requestRepository.findByEvent_IdAndState(eventId, RequestState.PENDING);
            requestsToReject.forEach(r -> r.setState(RequestState.REJECTED));
            requestRepository.saveAll(requestsToReject);
        }
        return requestMapper.toParticipationRequestDto(req);
    }

    @Transactional
    public ParticipationRequestDto rejectRequestByInitiator(Long userId, Long eventId, Long reqId) {
        userService.getUserById(userId);
        getEventById(eventId);
        Request req = requestService.getRequestById(reqId);
        if (req.getState() == RequestState.REJECTED)
            throw new ValidationConflictException(String
                    .format("Request id=%s already in status=%s. Reject is not possible.", reqId, req.getState()));
        req.setState(RequestState.REJECTED);
        req = requestRepository.save(req);
        return requestMapper.toParticipationRequestDto(req);
    }

    public List<EventFullDto> findByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                          String rangeStart, String rangeEnd, Integer from, Integer size) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
        Pageable pageable = CustomPageRequest.of(from, size);
        QEvent qEvent = QEvent.event;

        BooleanBuilder findCriteria = new BooleanBuilder();
        if (users != null)
            findCriteria.and(qEvent.initiator.id.in(users));
        if (states != null) {
            List<EventState> eventStates = EventState.convertList(states);
            if (eventStates.size() > 0)
                findCriteria.and(qEvent.state.in(eventStates));
        }
        if (categories != null)
            findCriteria.and(qEvent.category.id.in(categories));
        if (rangeStart != null) {
            LocalDateTime start;
            try {
                start = LocalDateTime.parse(rangeStart, dtf);
            } catch (IllegalArgumentException e) {
                throw new ValidationDataException(String.format("Invalid data rangeStart=%s", rangeStart));
            }
            findCriteria.and(qEvent.eventDate.after(start));
        }
        if (rangeEnd != null) {
            LocalDateTime end;
            try {
                end = LocalDateTime.parse(rangeEnd, dtf);
            } catch (IllegalArgumentException e) {
                throw new ValidationDataException(String.format("Invalid data rangeEnd=%s", rangeEnd));
            }
            findCriteria.and(qEvent.eventDate.before(end));
        }
        Page<Event> events = eventRepository.findAll(findCriteria, pageable);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateByAdmin(AdminUpdateEventRequest updateEventDto, Long eventId) {
        Event event = getEventById(eventId);
        if (updateEventDto.getAnnotation() != null)
            event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getCategory() != null)
            event.setCategory(categoryService.getCategoryById(updateEventDto.getCategory()));
        if (updateEventDto.getDescription() != null)
            event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getEventDate() != null)
            event.setEventDate(updateEventDto.getEventDate());
        if (updateEventDto.getLocation() != null) {
            event.setLocationLat(updateEventDto.getLocation().getLat());
            event.setLocationLon(updateEventDto.getLocation().getLon());
        }
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

    @Transactional
    public EventFullDto confirmRequestByAdmin(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getEventDate().minusHours(tbeForAdmin).isBefore(LocalDateTime.now()))
            throw new ValidationConflictException(String
                    .format("Event time cannot be set less than %s hour(s) from the current moment.", tbeForAdmin));
        if (event.getState() != EventState.PENDING)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Confirm is not possible.", eventId, event.getState()));
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto reject(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Reject is not possible.", eventId, event.getState()));
        event.setState(EventState.CANCELED);
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
    }
}
