package ru.practicum.ewmservice.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.CommentMapper;
import ru.practicum.ewmservice.model.Comment;
import ru.practicum.ewmservice.model.QComment;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.CommentDto;
import ru.practicum.ewmservice.model.dto.NewCommentDto;
import ru.practicum.ewmservice.model.dto.UpdateCommentDto;
import ru.practicum.ewmservice.storage.CommentRepository;
import ru.practicum.ewmservice.util.CustomPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    public CommentDto findById(Long commentId) {
        Comment comment = getCommentById(commentId);
        return CommentMapper.toCommentDto(comment);
    }

    public List<CommentDto> findByEventId(Long eventId, Integer from, Integer size, String sortStr) {
        eventService.getEventById(eventId);
        BooleanExpression findCriteria = QComment.comment.eventId.eq(eventId);
        Sort sort = "asc".equalsIgnoreCase(sortStr) ?
                Sort.by("created").ascending() :
                Sort.by("created").descending();
        Pageable pageable = CustomPageRequest.of(from, size, sort);
        return commentRepository.findAll(findCriteria, pageable).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Comment getCommentById(Long commentId) {
        return getComment(commentId);
    }

    @Transactional
    public CommentDto add(NewCommentDto commentDto, Long userId) {
        User commentator = userService.getUserById(userId);
        eventService.getEventById(commentDto.getEvent());
        Comment comment = CommentMapper.toComment(commentDto, commentator);
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    public CommentDto update(UpdateCommentDto commentDto, Long userId) {
        Comment comment = getCommentById(commentDto.getId());
        checkAuthor(comment, userId);
        comment.setText(commentDto.getText()); // Пустым быть не может, не проверяем.
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    public List<CommentDto> findByCommentator(Long userId, Integer from, Integer size, String sortStr) {
        userService.getUserById(userId);
        BooleanExpression findCriteria = QComment.comment.commentator.id.eq(userId);
        Sort sort = "asc".equalsIgnoreCase(sortStr) ?
                Sort.by("created").ascending() :
                Sort.by("created").descending();
        Pageable pageable = CustomPageRequest.of(from, size, sort);
        return commentRepository.findAll(findCriteria, pageable).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void remove(Long commentId, Long userId) {
        Comment comment = getCommentById(commentId);
        checkAuthor(comment, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void removeByAdmin(Long commentId) {
        try {
            commentRepository.deleteById(commentId);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationNotFoundException(String
                    .format("Comment with id=%s not found.", commentId));
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Comment with id=%s not found.", commentId)));
    }

    private static void checkAuthor(Comment comment, Long userId) {
        if (!userId.equals(comment.getCommentator().getId()))
            throw new ValidationForbiddenException(String
                    .format("User id=%s is not the author of comment id=%s", userId, comment.getId()));
    }
}
