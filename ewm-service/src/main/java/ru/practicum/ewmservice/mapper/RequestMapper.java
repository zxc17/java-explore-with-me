package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.dto.ParticipationRequestDto;

public class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getState().toString())
                .build();
    }
}
