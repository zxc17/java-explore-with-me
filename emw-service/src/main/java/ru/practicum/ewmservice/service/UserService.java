package ru.practicum.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.UserMapper;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.NewUserRequest;
import ru.practicum.ewmservice.model.dto.UserDto;
import ru.practicum.ewmservice.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public List<UserDto> find(List<Long> ids, Integer from, Integer size) {
        List<UserDto> result = new ArrayList<>();
        Optional<User> user;
        if (ids == null) {
            Pageable pageable = PageRequest.of(from / size, size);
            result = userRepository.findAll(pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            for (Long id : ids) {
                user = userRepository.findById(id);
                if (user.isPresent()) {     // Если такого ID нет, то не учитываем.
                    if (from <= 0) {        // Пропускаем "from" записей.
                        result.add(userMapper.toUserDto(user.get()));
                        if (--size <= 0)    // Сохраняем "size" записей,
                            break;          // и выходим.
                    } else from--;
                }
            }
        }
        return result;
    }

    public UserDto add(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    public void remove(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String.format("User with id=%s not found.", userId));
        userRepository.deleteById(userId);
    }
}
