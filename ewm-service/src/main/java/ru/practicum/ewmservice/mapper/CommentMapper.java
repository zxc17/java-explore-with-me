package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.Comment;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.CommentDto;
import ru.practicum.ewmservice.model.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CommentMapper {

    public static Comment toComment(NewCommentDto commentDto, User commentator) {
        return Comment.builder()
                .event_id(commentDto.getEvent())
                .commentator(commentator)
                .text(commentDto.getText())
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent_id())
                .commentator(UserMapper.toUserDto(comment.getCommentator()))
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }
}
