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
import ru.practicum.ewmservice.model.dto.CommentDto;
import ru.practicum.ewmservice.model.dto.NewCommentDto;
import ru.practicum.ewmservice.model.dto.UpdateCommentDto;
import ru.practicum.ewmservice.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;

    /* ******************* */
    /* *** PUBLIC PART *** */
    /* ******************* */

    @GetMapping("/comments/{commentId}")
    public CommentDto findById(@PathVariable Long commentId) {
        log.info("Endpoint 'Find comments by eventID' " +
                "commentID={}.", commentId);
        return commentService.findById(commentId);
    }

    @GetMapping("/comments/event/{eventId}")
    public List<CommentDto> findByEventId(
            @PathVariable Long eventId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "desc") String sort
    ) {
        log.info("Endpoint 'Find comments for event' " +
                "eventID={}, from={}, size={}, sort={}.", eventId, from, size, sort);
        return commentService.findByEventId(eventId, from, size, sort);
    }


    /* ******************** */
    /* *** PRIVATE PART *** */
    /* ******************** */

    @PostMapping("/users/{userId}/comments")
    public CommentDto add(
            @Validated @RequestBody NewCommentDto commentDto,
            @PathVariable Long userId
    ) {
        log.info("Endpoint 'Add comment' " +
                "RequestBody={}, userID={}.", commentDto, userId);
        return commentService.add(commentDto, userId);
    }

    @PatchMapping("/users/{userId}/comments")
    public CommentDto update(
            @Validated @RequestBody UpdateCommentDto updateCommentDto,
            @PathVariable Long userId
    ) {
        log.info("Endpoint 'Update comment' " +
                "RequestBody={}, userID={}.", updateCommentDto, userId);
        return commentService.update(updateCommentDto, userId);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> findByCommentator(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "desc") String sort
    ) {
        log.info("Endpoint 'Find comments by commentator' " +
                "userID={}, from={}, size={}, sort={}.", userId, from, size, sort);
        return commentService.findByCommentator(userId, from, size, sort);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    public void remove(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.info("Endpoint 'Remove comment' " +
                "commentID={}, userid={}.", commentId, userId);
        commentService.remove(commentId, userId);
    }


    /* ****************** */
    /* *** ADMIN PART *** */
    /* ****************** */

    @DeleteMapping("/admin/comments/{commentId}")
    public void removeByAdmin(@PathVariable Long commentId) {
        log.info("Endpoint 'Remove comment by admin' " +
                "commentID={}", commentId);
        commentService.removeByAdmin(commentId);
    }

}
