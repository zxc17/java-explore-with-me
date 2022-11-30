package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.CompilationDto;
import ru.practicum.ewmservice.model.dto.NewCompilationDto;
import ru.practicum.ewmservice.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class CompilationController {
    private final CompilationService compilationService;


              /* ******************* */
              /* *** PUBLIC PART *** */
              /* ******************* */

    @GetMapping("/compilations/{compId}")
    public CompilationDto findById(@PathVariable Long compId) {
        log.info("Endpoint 'Find compilation by ID' " +
                "compilationID={}.", compId);
        return compilationService.findById(compId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> findAll(
            @RequestParam(required = false) Optional<Boolean> pinned,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Endpoint 'Find compilations' " +
                "pinned={}, from={}, size={}.", pinned, from, size);
        return compilationService.findAll(pinned, from, size);
    }


              /* ****************** */
              /* *** ADMIN PART *** */
              /* ****************** */

    @PostMapping("/admin/compilations")
    public CompilationDto add(@Validated @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Endpoint 'Add compilation' " +
                "newCompilationDto={}.", newCompilationDto);
        return compilationService.add(newCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public void remove(@PathVariable Long compId) {
        log.info("Endpoint 'remove compilation' " +
                "compilationID={}.", compId);
        compilationService.remove(compId);
    }

    @DeleteMapping("/admin/compilations/{compId}/events/{eventId}")
    public void removeEvent(
            @PathVariable Long compId,
            @PathVariable Long eventId
    ) {
        log.info("Endpoint 'Remove event from compilation' " +
                "compilationID={}, eventID={}.", compId, eventId);
        compilationService.removeEvent(compId, eventId);
    }

    @PatchMapping("/admin/compilations/{compId}/events/{eventId}")
    public void addEvent(
            @PathVariable Long compId,
            @PathVariable Long eventId
    ) {
        log.info("Endpoint 'Add event to compilation' " +
                "compilationID={}, eventID={}.", compId, eventId);
        compilationService.addEvent(compId, eventId);
    }

    @PatchMapping("/admin/compilations/{compId}/pin")
    public void pin(@PathVariable Long compId) {
        log.info("Endpoint 'Pin collection to homepage' " +
                "compilationID={}.", compId);
        compilationService.pin(compId);
    }

    @DeleteMapping("/admin/compilations/{compId}/pin")
    public void unpin(@PathVariable Long compId) {
        log.info("Endpoint 'Unpin collection to homepage' " +
                "compilationID={}.", compId);
        compilationService.unpin(compId);
    }

}
