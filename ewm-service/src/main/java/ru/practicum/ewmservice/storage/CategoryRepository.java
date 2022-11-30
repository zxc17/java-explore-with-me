package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
