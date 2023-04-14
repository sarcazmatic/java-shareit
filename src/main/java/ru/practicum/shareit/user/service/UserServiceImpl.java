package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.checkExistEmail(userDto.getEmail()))
            throw new ConflictException("Пользователь с таким email уже существует!");

        User user = userRepository.create(UserMapper.fromUserDtoToUser(userDto));

        return UserMapper.fromUserToUserDto(user);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        if (!userRepository.checkExistId(id)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        User userToUpdate = userRepository.read(id);

        if (userRepository.checkExistEmail(userDto.getEmail())) {
            if (!userToUpdate.getEmail().equals(userDto.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже существует!");
            }
        }

        if (userDto.getName() != null) {
            userToUpdate.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            userToUpdate.setEmail(userDto.getEmail());
        }

        return UserMapper.fromUserToUserDto(userToUpdate);
    }

    @Override
    public UserDto getById(long id) {
        if (!userRepository.checkExistId(id)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        return UserMapper.fromUserToUserDto(userRepository.read(id));
    }

    @Override
    public void delete(long id) {
        if (!userRepository.checkExistId(id)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        User user = userRepository.read(id);

        userRepository.deleteEmailFromSet(user.getEmail());
        userRepository.delete(id);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.getAll();

        return users.stream()
                .map(user -> UserMapper.fromUserToUserDto(user))
                .collect(Collectors.toList());
    }
}
