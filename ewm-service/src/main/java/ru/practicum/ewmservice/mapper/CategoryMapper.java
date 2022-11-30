package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.dto.CategoryDto;
import ru.practicum.ewmservice.model.dto.NewCategoryDto;

public class CategoryMapper {

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
