package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.AdminUpdateEventRequest;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.model.dto.UpdateEventRequest;
import ru.practicum.ewmservice.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;


              /* ******************* */
              /* *** PUBLIC PART *** */
              /* ******************* */

    @GetMapping("/events/{id}")
    public EventFullDto findById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Endpoint 'Find event by id', public part; " +
                "eventID={}.", id);
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        log.info("endpoint path: {}", uri);
        log.info("client ip: {}", ip);
        return eventService.findById(id, uri, ip);
    }

    @GetMapping("/events")
    public List<EventFullDto> findAll(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @Validated @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Validated @Positive @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        log.info("Endpoint 'Find events', public part. ");
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        log.info("endpoint path: {}", uri);
        log.info("client ip: {}", ip);
        return eventService.findAll(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                from, size,
                uri, ip);
    }


              /* ******************** */
              /* *** PRIVATE PART *** */
              /* ******************** */

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> findAllPrivate(
            @PathVariable Long userId,
            @Validated @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Validated @Positive @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Endpoint 'Find events made by current user' " +
                "userID={}.", userId);
        return eventService.findAllPrivate(userId, from, size);
    }

    @PatchMapping("/users/{userId}/events")
    public EventFullDto update(
            @PathVariable Long userId,
            @Validated @RequestBody UpdateEventRequest updateEventRequest
    ) {
        log.info("Endpoint 'Update event by initiator' " +
                "RequestBody={}, userID={}.", updateEventRequest, userId);
        return eventService.update(updateEventRequest, userId);
    }

    @PostMapping("/users/{userId}/events")
    public EventFullDto add(
            @PathVariable Long userId,
            @Validated @RequestBody NewEventDto newEventDto
    ) {
        log.info("Endpoint 'Add event' " +
                "RequestBody={}, userID={}.", newEventDto, userId);
        if (newEventDto.getPaid() == null)
            newEventDto.setPaid(false);
        if (newEventDto.getParticipantLimit() == null)
            newEventDto.setParticipantLimit(0);
        if (newEventDto.getRequestModeration() == null)
            newEventDto.setRequestModeration(true);
        return eventService.add(newEventDto, userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto findById(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("Endpoint 'Find event by ID for current user' " +
                "userID={}, eventID={}.", userId, eventId);
        return eventService.findByIdPrivate(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto cancel(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("Endpoint 'Cancel event for current user' " +
                "userID={}, eventID={}.", userId, eventId);
        return eventService.cancel(userId, eventId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestToEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("Endpoint 'Find requests for an event made by current user' " +
                "userID={}, eventID={}.", userId, eventId);
        return eventService.findRequestsToEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmRequestByInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long reqId
    ) {
        log.info("Endpoint 'Confirm an event request by initiator' " +
                "initiatorID={}, eventID={}, requestID={}.", userId, eventId, reqId);
        return eventService.confirmRequestByInitiator(userId, eventId, reqId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectRequestByInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long reqId
    ) {
        log.info("Endpoint 'Reject an event request by initiator' " +
                "initiatorID={}, eventID={}, requestID={}.", userId, eventId, reqId);
        return eventService.rejectRequestByInitiator(userId, eventId, reqId);
    }


              /* ****************** */
              /* *** ADMIN PART *** */
              /* ****************** */

    @GetMapping("/admin/events")
    public List<EventFullDto> findByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @Validated @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Validated @Positive @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Endpoint 'Find events by admin' " +
                        "users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}.",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.findByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/admin/events/{eventId}")
    public EventFullDto updateByAdmin(
            @PathVariable Long eventId,
            @RequestBody AdminUpdateEventRequest updateEventDto
    ) {
        log.info("Endpoint 'Update event by admin' " +
                "eventID={}, RequestBody={}.", eventId, updateEventDto);
        return eventService.updateByAdmin(updateEventDto, eventId);
    }

    @PatchMapping("/admin/events/{eventId}/publish")
    public EventFullDto confirmRequestByAdmin(@PathVariable Long eventId) {
        log.info("Endpoint 'Confirm event' " +
                "eventID={}.", eventId);
        return eventService.confirmRequestByAdmin(eventId);
    }

    @PatchMapping("/admin/events/{eventId}/reject")
    public EventFullDto reject(@PathVariable Long eventId) {
        log.info("Endpoint 'Reject event' " +
                "eventID={}.", eventId);
        return eventService.reject(eventId);
    }
}
