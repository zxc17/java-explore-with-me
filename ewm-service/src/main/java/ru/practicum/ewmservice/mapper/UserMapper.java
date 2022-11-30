package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.NewUserRequest;
import ru.practicum.ewmservice.model.dto.UserDto;
import ru.practicum.ewmservice.model.dto.UserShortDto;

public class UserMapper {

    public static User toUser(NewUserRequest userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
