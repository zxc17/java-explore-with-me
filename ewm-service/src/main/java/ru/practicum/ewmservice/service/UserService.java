package ru.practicum.ewmservice.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;
import ru.practicum.ewmservice.mapper.UserMapper;
import ru.practicum.ewmservice.model.QUser;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.model.dto.NewUserRequest;
import ru.practicum.ewmservice.model.dto.UserDto;
import ru.practicum.ewmservice.storage.UserRepository;
import ru.practicum.ewmservice.util.CustomPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public List<UserDto> find(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = CustomPageRequest.of(from, size);
        QUser qUser = QUser.user;
        BooleanBuilder findCriteria = new BooleanBuilder();
        if (ids != null && ids.size() > 0)
            findCriteria.and(qUser.id.in(ids));
        return userRepository.findAll(findCriteria, pageable).stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());

    }

    @Transactional
    public UserDto add(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Transactional
    public void remove(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ValidationNotFoundException(String.format("User with id=%s not found.", userId));
        userRepository.deleteById(userId);
    }

    User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ValidationNotFoundException(String
                        .format("User with id=%s not found.", userId)));
    }
}
