package ru.practicum.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.CategoryMapper;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.dto.CategoryDto;
import ru.practicum.ewmservice.model.dto.NewCategoryDto;
import ru.practicum.ewmservice.storage.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    public List<CategoryDto> findAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto findById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Category with id=%s not found.", catId)));
        return categoryMapper.toCategoryDto(category);
    }

    public CategoryDto update(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Category with id=%s not found.", categoryDto.getId())));
        // Пустым быть не может, не проверяем.
        category.setName(categoryDto.getName());
        // Контроль уникальности в БД. Обработка исключения в ErrorHandler.
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    public CategoryDto add(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        // Контроль уникальности в БД. Обработка исключения в ErrorHandler.
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    public void remove(Long catId) {
        if (!categoryRepository.existsById(catId))
            throw new ValidationNotFoundException(String
                    .format("Category with id=%s not found.", catId));
        categoryRepository.deleteById(catId);
    }
}
