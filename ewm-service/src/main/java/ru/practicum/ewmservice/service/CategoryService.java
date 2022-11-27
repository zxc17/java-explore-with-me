package ru.practicum.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.CategoryMapper;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.dto.CategoryDto;
import ru.practicum.ewmservice.model.dto.NewCategoryDto;
import ru.practicum.ewmservice.storage.CategoryRepository;
import ru.practicum.ewmservice.util.CustomPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    public List<CategoryDto> findAll(Integer from, Integer size) {
        Pageable pageable = CustomPageRequest.of(from, size);
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto findById(Long catId) {
        Category category = getCategoryById(catId);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category category = getCategoryById(categoryDto.getId());
        category.setName(categoryDto.getName());
        // Контроль уникальности в БД. Обработка исключения в ErrorHandler.
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    public CategoryDto add(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        // Контроль уникальности в БД. Обработка исключения в ErrorHandler.
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    public void remove(Long catId) {
        getCategoryById(catId);
        categoryRepository.deleteById(catId);
    }

    Category getCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("Category with id=%s not found.", catId)));
    }

}
