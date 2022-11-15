package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.model.dto.NewUserRequest;
import ru.practicum.ewmservice.model.dto.UserDto;
import ru.practicum.ewmservice.service.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
public class UserController {
    private final UserService userService;

    /* *** PUBLIC PART *** */

    /* *** PRIVATE PART *** */

    /* *** ADMIN PART *** */

    @GetMapping("/admin/users")
    public List<UserDto> find(
            @RequestParam(required = false) List<Long> ids,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Endpoint 'Find users' " +
                "RequestBody={}, from={}, size={}.", ids, from, size);
        return userService.find(ids, from, size);
    }

    @PostMapping("/admin/users")
    public UserDto add(@Validated @RequestBody NewUserRequest newUserRequest) {
        log.info("Endpoint 'Adding new user' " +
                "RequestBody={}.", newUserRequest);
        return userService.add(newUserRequest);
    }

    @DeleteMapping("/admin/users/{userId}")
    public void remove(@PathVariable Long userId) {
        log.info("Endpoint 'Remove user' " +
                "userId={}.", userId);
        userService.remove(userId);
    }
}
