package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.model.Request;
import ru.practicum.ewmservice.model.RequestState;

import java.util.List;

public interface RequestRepository  extends JpaRepository<Request, Long> {

    List<Request> findByEvent_IdAndState(Long eventId, RequestState requestState);

    long countByEvent_IdAndState(Long eventId, RequestState requestState);

    List<Request> findByRequester_Id(Long requesterId);
}
