package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
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
    public User createUser(UserDto userDto) throws ConflictException {

        User user = UserMapper.fromDtoToUser(userDto);

            User createdUser = userDbStorage.save(user);
            log.info("Пользователь с почтой {} был создан", user.getEmail());
            return createdUser;

    }

    @Transactional
    public User updateUser(Long userId, UserDto userDto) {
        User user = UserMapper.fromDtoToUser(userDto);
        User updatedUser = getUserById(userId);

        String updatedName = user.getName();
        if (updatedName != null && !updatedName.isBlank())
            updatedUser.setName(updatedName);

        String updatedEmail = user.getEmail();

        if (updatedEmail != null && !updatedEmail.isBlank()) {
            updatedUser.setEmail(updatedEmail);
        }
        return updatedUser;
    }

    @Override
    public void deleteUserById(Long userId) {
        userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        userDbStorage.deleteById(userId);
    }

}
