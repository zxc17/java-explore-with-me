package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.CategoryDto;
import ru.practicum.ewmservice.model.dto.NewCategoryDto;
import ru.practicum.ewmservice.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
public class CategoryController {
    private final CategoryService categoryService;


    /* ******************* */
    /* *** PUBLIC PART *** */
    /* ******************* */

    @GetMapping("/categories")
    public List<CategoryDto> findAll(
            @PositiveOrZero @RequestParam Integer from,
            @Positive @RequestParam Integer size
    ) {
        log.info("Endpoint 'Find categories' " +
                "from={}, size={}.", from, size);
        return categoryService.findAll(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto findById(@PathVariable Long catId) {
        log.info("Endpoint 'Find category by id' " +
                "categoryID={}", catId);
        return categoryService.findById(catId);
    }


    /* ******************** */
    /* *** PRIVATE PART *** */
    /* ******************** */



    /* ****************** */
    /* *** ADMIN PART *** */
    /* ****************** */

    @PatchMapping("/admin/categories")
    public CategoryDto update(@Validated @RequestBody CategoryDto categoryDto) {
        log.info("Endpoint 'Update category' " +
                "RequestBody={}.", categoryDto);
        return categoryService.update(categoryDto);
    }

    @PostMapping("/admin/categories")
    public CategoryDto add(@Validated @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Endpoint 'Adding new category' " +
                "RequestBody={}.", newCategoryDto);
        return categoryService.add(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    public void remove(@PathVariable Long catId) {
        log.info("Endpoint 'Remove category' " +
                "categoryId={}.", catId);
        categoryService.remove(catId);
    }
}
