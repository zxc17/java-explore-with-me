package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.EventFullDto;
import ru.practicum.ewmservice.model.dto.EventShortDto;
import ru.practicum.ewmservice.model.dto.NewEventDto;
import ru.practicum.ewmservice.model.dto.UpdateEventRequest;
import ru.practicum.ewmservice.service.EventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
public class EventController {
    private final EventService eventService;


    /* ******************* */
    /* *** PUBLIC PART *** */
    /* ******************* */


    /* ******************** */
    /* *** PRIVATE PART *** */
    /* ******************** */

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> findAllByCurrentUser(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Endpoint 'Find events made by current user' " +
                "userID={}.", userId);
        return eventService.findAllByCurrentUser(userId, from, size);
    }

    @PatchMapping("/users/{userId}/events")
    public EventFullDto update(
            @PathVariable Long userId,
            @RequestBody UpdateEventRequest updateEventRequest
    ) {
        log.info("Endpoint 'Update events by initiator' " +
                "RequestBody={}, userID={}.", updateEventRequest, userId);
        return eventService.update(updateEventRequest, userId);
    }

    @PostMapping("/users/{userId}/events")
    public EventFullDto add(
            @PathVariable Long userId,
            @RequestBody NewEventDto newEventDto
    ) {
        log.info("Endpoint 'Add events' " +
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
        return eventService.findById(userId, eventId);
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


    /* ****************** */
    /* *** ADMIN PART *** */
    /* ****************** */


}
