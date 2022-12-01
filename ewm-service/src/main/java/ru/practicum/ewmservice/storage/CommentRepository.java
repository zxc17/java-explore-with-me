package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmservice.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("  select c from Comment as c " +
            " where c.event_id = ?1 ")
    List<Comment> findByEventId(Long eventId);

    List<Comment> findByCommentator_Id(Long userId);
}
