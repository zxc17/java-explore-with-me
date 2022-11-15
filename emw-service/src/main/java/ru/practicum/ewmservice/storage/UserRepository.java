package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
