package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userDbStorage;

    @Override
    public User getUserById(Long userId) {
        return userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
    }

    @Override
    public List<User> getAllUsersDto() {
        return userDbStorage.findAll();

    }

    @Transactional
    public User createUser(UserDto userDto) {

        User user = UserMapper.fromDtoToUser(userDto);

        if (user.getEmail() == null)
            throw new ValidationException("Почта не найдена");

        if (!userDbStorage.existsByEmailAndId(user.getEmail(), user.getId())) {
            log.info("Пользователь с почтой {} был создан", user.getEmail());
            return userDbStorage.save(user);
        } else {
            log.warn("Пользователь с почтой {} уже существует", user.getEmail());
            throw new ConflictException("Пользователь уже существует");
        }

    }

    @Transactional
    public User updateUser(Long userId, UserDto userDto) {
        User user = UserMapper.fromDtoToUser(userDto);
        User updatedUser = getUserById(userId);

        String updatedName = user.getName();
        if (updatedName != null && !updatedName.isBlank())
            updatedUser.setName(updatedName);

        String updatedEmail = user.getEmail();

        if (updatedEmail != null && !updatedEmail.isBlank())
            updatedUser.setEmail(updatedEmail);

        return updatedUser;
    }

    @Override
    public void deleteUserById(Long userId) {
        userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        userDbStorage.deleteById(userId);
    }

}
