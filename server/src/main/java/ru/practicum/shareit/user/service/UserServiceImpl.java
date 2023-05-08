package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (user.getEmail() != null) {
            try {
                log.info("User with email {} was created", user.getEmail());
                User createdUser = userRepository.save(user);
                return UserMapper.toUserDto(createdUser);
            } catch (RuntimeException e) {
                log.warn("User with email {} exists", user.getEmail());
                throw new ConflictException("User exists");
            }

        } else {
            throw new ValidationException("Email not found");
        }
    }

    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User userFromDto = UserMapper.toUser(userDto);
        getUserById(userId);
        User updatedUser = getUserValid(userId, userFromDto);
        log.info("Updated user {}", userFromDto);
        userRepository.save(updatedUser);
        return UserMapper.toUserDto(updatedUser);
    }

    public void removeUserById(Long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID %s not found", userId)));
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> retrieveAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private User getUserValid(long userId, User user) {
        User updatedUser = UserMapper.toUser(getUserById(userId));

        String updatedName = user.getName();
        if (updatedName != null && !updatedName.isBlank())
            updatedUser.setName(updatedName);

        String updatedEmail = user.getEmail();
        if (updatedEmail != null && !updatedEmail.isBlank()) {

/*            if (userRepository.findByEmail(updatedEmail).isPresent()) {
                throw new ConflictException("User exists");
            }*/

            updatedUser.setEmail(updatedEmail);
        }
        return updatedUser;
    }
}
