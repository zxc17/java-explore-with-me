package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
