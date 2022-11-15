package ru.practicum.ewmservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.RequestState;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.UserRepository;

@Component
@RequiredArgsConstructor
public class RequestMapper {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public Request toRequest(ParticipationRequestDto requestDto) {
        return Request.builder()
                .requester(userRepository.findById(requestDto.getRequester()).get())
                .event(eventRepository.findById(requestDto.getEvent()).get())
                .created(requestDto.getCreated())
                .state(RequestState.valueOf(requestDto.getStatus()))
                .build();
    }

    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getState().toString())
                .build();
    }
}
