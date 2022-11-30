package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewmservice.model.Compilation;

public interface CompilationRepository extends
        JpaRepository<Compilation, Long>,
        QuerydslPredicateExecutor<Compilation> {
}
