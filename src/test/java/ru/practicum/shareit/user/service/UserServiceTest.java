package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Autowired
    private final UserService userService;
    private User user1 = new User(1L, "user1", "user1@mail.ru");
    private User user2 = new User(2L, "user2", "user2@mail.ru");
    private User user3 = new User(3L, "user3", "user3@mail.ru");
    private User user4 = new User(4L, "user4", "user3@mail.ru");

    @Autowired
    public UserServiceTest(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        this.userService = userService;
    }

    @Test
    void getUserByIdTest() {
        assertEquals(UserMapper.userToDto(user1).getId(), userService.getUserById(1L).getId());
    }

    @Test
    void retrieveAllUsersTest() {
        assertEquals(3, userService.getAllUsersDto().size());
    }

    @Test
    void removeUserByIdTest() {
        userService.deleteUserById(1L);
        assertNull(userRepository.findById(1L).orElse(null));
    }

    @Test
    void updateUserTest() {
        UserDto userDto = UserMapper.userToDto(userService.getUserById(1L));
        userDto.setName("newName");
        userDto.setEmail("newmail@test.ru");
        userService.updateUser(1L, userDto);
        assertEquals("newName", userService.getUserById(1L).getName());
        assertEquals("newmail@test.ru", userService.getUserById(1L).getEmail());
    }

    @Test
    void createUserTest() {
        assertEquals(userService.getUserById(UserMapper.userToDto(user3).getId()).getId(), user3.getId());
    }

    @Test
    void createDoubleUserTest() {
        assertThrows(ConflictException.class, () -> userService.createUser(UserMapper.userToDto(user4)));
    }

}
