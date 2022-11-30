package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.Compilation;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.dto.CompilationDto;
import ru.practicum.ewmservice.model.dto.NewCompilationDto;

import java.util.List;
import java.util.Map;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(EventMapper.toEventShortDto(compilation.getEvents(), views))
                .build();
    }
}
