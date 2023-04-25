package ru.practicum.shareit.user.repository;


import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    boolean checkExistId(long id);

    boolean checkExistEmail(String email);

    void deleteEmailFromSet(String email);

    User create(User user);

    User read(long id);

    void delete(long id);

    List<User> getAll();
}
