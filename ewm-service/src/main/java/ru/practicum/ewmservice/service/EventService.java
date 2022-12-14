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
import ru.practicum.ewmservice.model.Category;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewmservice.mapper.EventMapper.toEventFullDto;
import static ru.practicum.ewmservice.util.Constants.DATE_PATTERN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    @Value("${time-before-event}")
    int tbe;
    @Value("${time-before-event.admin}")
    int tbeForAdmin;
    @Value("${app-name}")
    String appName;

    public List<EventFullDto> findAll(String text,
                                      List<Long> categories,
                                      Boolean paid,
                                      String rangeStart,
                                      String rangeEnd,
                                      Boolean onlyAvailable,
                                      String sortStr,
                                      Integer from,
                                      Integer size,
                                      String uri,
                                      String ip) {
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
        statsClient.sendHit(new EndpointHit(appName, uri, ip));
        List<EventFullDto> result;
        Map<Long, Long> views;
        Page<Event> events;
        Pageable pageable;
        switch (Optional.ofNullable(EventSort.convert(sortStr)).orElse(EventSort.UNSORTED)) {
            case EVENT_DATE:
                Sort sort = Sort.by("eventDate");
                pageable = CustomPageRequest.of(from, size, sort);
                events = eventRepository.findAll(findCriteria, pageable);
                views = statsClient.getStats(getIds(events));
                result = toEventFullDto(events, views);
                break;
            case VIEWS:
                Iterable<Event> eventIterable = eventRepository.findAll(findCriteria);
                views = statsClient.getStats(getIds(eventIterable));
                result = toEventFullDto(eventIterable, views).stream()
                        .sorted(Comparator.comparing(EventFullDto::getViews))
                        .collect(Collectors.toList());
                break;
            default: //??????????????????????????????
                pageable = CustomPageRequest.of(from, size);
                events = eventRepository.findAll(findCriteria, pageable);
                views = statsClient.getStats(getIds(events));
                result = toEventFullDto(events, views);
        }
        return result;
    }

    public EventFullDto findById(Long eventId, String uri, String ip) {
        Event event = getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Event id=%s not published", eventId));
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        statsClient.sendHit(new EndpointHit(appName, uri, ip));
        return toEventFullDto(event, views);
    }

    public List<EventShortDto> findAllPrivate(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = CustomPageRequest.of(from, size);
        List<Event> events = eventRepository.findByInitiator_Id(userId, pageable);
        Map<Long, Long> views = statsClient.getStats(getIds(events));
        return EventMapper.toEventShortDto(events, views);
    }

    @Transactional
    public EventFullDto update(UpdateEventRequest updateEventRequest, Long userId) {
        userService.getUserById(userId);
        Event event = getEventById(updateEventRequest.getEventId());
        checkInitiator(event, userId);
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
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(event.getId()));
        return toEventFullDto(event, views);
    }

    @Transactional
    public EventFullDto add(NewEventDto newEventDto, Long userId) {
        User initiator = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        Event event = EventMapper.toEvent(newEventDto, category);
        if (event.getEventDate().minusHours(tbe).isBefore(LocalDateTime.now()))
            throw new ValidationConflictException(String
                    .format("Event time cannot be set less than %s hour(s) from the current moment.", tbe));
        event.setCreatedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(event.getId()));
        return toEventFullDto(event, views);
    }

    public EventFullDto findByIdPrivate(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        checkInitiator(event, userId);
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        return toEventFullDto(event, views);
    }

    @Transactional
    public EventFullDto cancel(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        checkInitiator(event, userId);
        if (event.getState() != EventState.PENDING)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Cancel is not possible.", eventId, event.getState()));
        event.setState(EventState.CANCELED);
        event = eventRepository.save(event);
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        return toEventFullDto(event, views);
    }

    public List<ParticipationRequestDto> findRequestsToEvent(Long userId, Long eventId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        checkInitiator(event, userId);
        return requestRepository.findByEvent_Id(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto confirmRequestByInitiator(Long userId, Long eventId, Long reqId) {
        userService.getUserById(userId);
        Event event = getEventById(eventId);
        checkInitiator(event, userId);
        Request req = requestService.getRequestById(reqId);
        if (req.getState() != RequestState.PENDING)
            throw new ValidationConflictException(String
                    .format("Request id=%s in status=%s. Confirm is not possible.", reqId, req.getState()));
        long confirmedRequest = event.getConfirmedMembers().size();
        if (event.getParticipantLimit() > 0 && confirmedRequest >= event.getParticipantLimit())
            throw new ValidationConflictException("The maximum number of participants has been reached.");
        req.setState(RequestState.CONFIRMED);
        req = requestRepository.save(req);
        // ???????? ?????????????????? ?????????? ????????????????????, ?????? ???????????????????????????????? ???????????? ??????????????????.
        if (event.getParticipantLimit() > 0 && ++confirmedRequest == event.getParticipantLimit()) {
            List<Request> requestsToReject = requestRepository.findByEvent_IdAndState(eventId, RequestState.PENDING);
            requestsToReject.forEach(r -> r.setState(RequestState.REJECTED));
            requestRepository.saveAll(requestsToReject);
        }
        return RequestMapper.toParticipationRequestDto(req);
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
        return RequestMapper.toParticipationRequestDto(req);
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
        Map<Long, Long> views = statsClient.getStats(getIds(events));
        return toEventFullDto(events, views);
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
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        return toEventFullDto(event, views);
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
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        return toEventFullDto(event, views);
    }

    @Transactional
    public EventFullDto reject(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Event id=%s in status=%s. Reject is not possible.", eventId, event.getState()));
        event.setState(EventState.CANCELED);
        event = eventRepository.save(event);
        Map<Long, Long> views = statsClient.getStats(Collections.singletonList(eventId));
        return toEventFullDto(event, views);
    }

    Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
    }

    private void checkInitiator(Event event, long userId) {
        if (userId != event.getInitiator().getId())
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the initiator of event id=%s", userId, event.getId()));
    }

    List<Long> getIds(Iterable<Event> events) {
        List<Long> result = new ArrayList<>();
        events.forEach(e -> result.add(e.getId()));
        return result;
    }
}
