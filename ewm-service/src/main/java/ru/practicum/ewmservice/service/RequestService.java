package ru.practicum.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.customException.ValidationConflictException;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.RequestMapper;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.EventState;
import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.RequestState;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.RequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserService userService;
    private final EventRepository eventRepository; //EventService подключать нельзя из-за цикличной зависимости.

    public List<ParticipationRequestDto> findByRequesterId(Long userId) {
        userService.getUserById(userId);
        return requestRepository.findByRequester_Id(userId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        User requester = userService.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Event with id=%s not found.", eventId)));
        if (userId.equals(event.getInitiator().getId()))
            throw new ValidationConflictException("Cannot submit a request to participate in own event.");
        if (event.getState() != EventState.PUBLISHED)
            throw new ValidationConflictException(String
                    .format("Requests are only accepted for published events. EventState=%s", event.getState()));
        if (event.getConfirmedMembers().size() >= event.getParticipantLimit())
            throw new ValidationConflictException("The maximum number of participants has been reached.");
        Request request = Request.builder()
                .requester(requester)
                .event(event)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .state(event.getRequestModeration() ? RequestState.PENDING : RequestState.CONFIRMED)
                .build();
        // Контроль уникальности в БД. Обработка исключения в ErrorHandler.
        request = requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(request);
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userService.getUserById(userId);
        Request request = getRequestById(requestId);
        if (!userId.equals(request.getRequester().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s not owner of request id=%s.", userId, requestId));
        request.setState(RequestState.CANCELED);
        request = requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(request);
    }

    Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Request with id=%s not found.", requestId)));
    }

}
