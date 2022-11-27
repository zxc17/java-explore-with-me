package ru.practicum.ewmservice.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.CompilationMapper;
import ru.practicum.ewmservice.model.Compilation;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.QCompilation;
import ru.practicum.ewmservice.model.dto.CompilationDto;
import ru.practicum.ewmservice.model.dto.NewCompilationDto;
import ru.practicum.ewmservice.storage.CompilationRepository;
import ru.practicum.ewmservice.util.CustomPageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;
    private final CompilationMapper compilationMapper;

    public CompilationDto findById(Long compId) {
        Compilation comp = getCompilationById(compId);
        return compilationMapper.toCompilationDto(comp);
    }

    public List<CompilationDto> findAll(Optional<Boolean> pinnedOptional, Integer from, Integer size) {
        BooleanExpression byPinned = pinnedOptional.isPresent() ?
                // Если параметр pinned задан - ищем заданное значение (закреплено или нет)
                QCompilation.compilation.pinned.eq(pinnedOptional.get()) :
                // Не задан - ищем все.
                Expressions.asBoolean(true).isTrue();
        Pageable pageable = CustomPageRequest.of(from, size);
        return compilationRepository.findAll(byPinned, pageable).stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        Compilation comp = compilationMapper.toCompilation(newCompilationDto);
        comp = compilationRepository.save(comp);
        return compilationMapper.toCompilationDto(comp);
    }

    @Transactional
    public void remove(Long compId) {
        getCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public void removeEvent(Long compId, Long eventId) {
        Compilation comp = getCompilationById(compId);
        Event event = eventService.getEventById(eventId);
        comp.getEvents().remove(event);
        compilationRepository.save(comp);
    }

    @Transactional
    public void addEvent(Long compId, Long eventId) {
        Compilation comp = getCompilationById(compId);
        Event event = eventService.getEventById(eventId);
        comp.getEvents().add(event);
        compilationRepository.save(comp);
    }

    @Transactional
    public void pin(Long compId) {
        Compilation comp = getCompilationById(compId);
        comp.setPinned(true);
        compilationRepository.save(comp);
    }

    @Transactional
    public void unpin(Long compId) {
        Compilation comp = getCompilationById(compId);
        comp.setPinned(false);
        compilationRepository.save(comp);
    }

    Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Compilation with id=%s not found.", compId)));
    }
}
