package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.service.RequestService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;


              /* ******************** */
              /* *** PRIVATE PART *** */
              /* ******************** */

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getByRequesterId(@PathVariable Long userId) {
        log.info("Endpoint 'Find requests for current user' " +
                "userID={}.", userId);
        return requestService.findByRequesterId(userId);
    }

    @PostMapping("/users/{userId}/requests")
    public ParticipationRequestDto add(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        log.info("Endpoint 'Add a request to participate in an event from the current user' " +
                "userID={}, eventID={}.", userId, eventId);
        return requestService.add(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Endpoint 'Cancel request for current user' " +
                "userID={}, requestID={}.", userId, requestId);
        return requestService.cancel(userId, requestId);
    }
}
