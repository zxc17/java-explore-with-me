package ru.practicum.ewmservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.dto.CategoryDto;
import ru.practicum.ewmservice.model.dto.NewCategoryDto;

@Component
public class CategoryMapper {

    public Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
